package snoopypupser.buyingchunks.network;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import snoopypupser.buyingchunks.BuyingChunks;
import snoopypupser.buyingchunks.claimshop.ClaimShopEntry;
import snoopypupser.buyingchunks.claimshop.ClaimShopSavedData;
import snoopypupser.buyingchunks.claimshop.ClaimShopSync;

import java.util.Optional;
import java.util.UUID;

public record BuyChunkPacket(int chunkX, int chunkZ) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BuyChunkPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BuyingChunks.MOD_ID, "buy_chunk"));

    public static final StreamCodec<FriendlyByteBuf, BuyChunkPacket> STREAM_CODEC =
            StreamCodec.of(BuyChunkPacket::encode, BuyChunkPacket::decode);

    private static void encode(FriendlyByteBuf buf, BuyChunkPacket packet) {
        buf.writeInt(packet.chunkX);
        buf.writeInt(packet.chunkZ);
    }

    private static BuyChunkPacket decode(FriendlyByteBuf buf) {
        return new BuyChunkPacket(buf.readInt(), buf.readInt());
    }

    // Generic error sound (no team, server team, not for sale)
    private static void playGenericError(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.VILLAGER_NO,
                SoundSource.PLAYERS,
                1.0f, 1.0f
        );
    }

    // No money sound (heavy, dull feeling)
    private static void playNoMoneyError(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ANVIL_LAND,
                SoundSource.PLAYERS,
                0.5f, 1.5f
        );
    }

    // Success sound (epic level up)
    private static void playSuccess(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS,
                0.5f, 1.0f
        );
    }

    public static void handle(BuyChunkPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerLevel level = (ServerLevel) player.level();
            ChunkPos pos = new ChunkPos(packet.chunkX(), packet.chunkZ());
            ChunkDimPos dimPos = new ChunkDimPos(level.dimension(), pos);

            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);
            ClaimShopEntry entry = savedData.getData().getEntry(pos);

            if (entry == null) {
                playGenericError(player);
                player.sendSystemMessage(Component.translatable("uc7core.claimshop.error.notforsale"));
                return;
            }

            ItemStack price = entry.getPrice();

            if (!hasEnoughItems(player, price)) {
                playNoMoneyError(player);
                player.sendSystemMessage(Component.translatable(
                        "uc7core.claimshop.error.notenoughitems",
                        price.getCount(),
                        price.getItem().getDescription().getString()
                ));
                return;
            }

            Optional<Team> buyerTeam = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            if (buyerTeam.isEmpty()) {
                playGenericError(player);
                player.sendSystemMessage(Component.translatable("uc7core.claimshop.error.noteam"));
                return;
            }

            Team team = buyerTeam.get();
            BuyingChunks.LOGGER.info("BuyChunk: buyer={} team={} isServer={}",
                    player.getGameProfile().getName(), team.getName().getString(), team.isServerTeam());

            if (team.isServerTeam()) {
                playGenericError(player);
                player.sendSystemMessage(Component.translatable("uc7core.claimshop.error.serverbuyerteam"));
                return;
            }

            // Chunk-Limit prüfen
            Optional<Team> shopTeamOpt = FTBTeamsAPI.api().getManager().getTeams().stream()
                    .filter(t -> t.getName().getString().equals(entry.getShopTeamName()))
                    .findFirst();

            if (shopTeamOpt.isPresent()) {
                UUID shopTeamId = shopTeamOpt.get().getId();
                if (!savedData.getData().canBuy(shopTeamId, team.getId())) {
                    playGenericError(player);
                    int limit = savedData.getData().getTeamChunkLimit(shopTeamId);
                    player.sendSystemMessage(Component.translatable(
                            "uc7core.claimshop.error.chunklimit",
                            limit,
                            entry.getShopTeamName()
                    ));
                    return;
                }
            }

            ClaimedChunkManager manager = FTBChunksAPI.api().getManager();

            var existingChunk = manager.getChunk(dimPos);
            if (existingChunk != null) {
                existingChunk.getTeamData().unclaim(
                        player.createCommandSourceStack(), dimPos, false
                );
            }

            removeItems(player, price);

            savedData.getData().removeFromSale(pos);

            // Kaufzähler erhöhen
            shopTeamOpt.ifPresent(shopTeam ->
                    savedData.getData().incrementBoughtCount(shopTeam.getId(), team.getId())
            );

            savedData.setDirty();

            manager.getOrCreateData(team).claim(
                    player.createCommandSourceStack(),
                    dimPos,
                    false
            );

            ClaimShopSync.syncToAll(player.getServer());

            playSuccess(player);

            player.sendSystemMessage(Component.translatable(
                    "uc7core.claimshop.buy.success",
                    pos.x, pos.z,
                    price.getCount(),
                    price.getItem().getDescription().getString()
            ));
        });
    }

    private static boolean hasEnoughItems(ServerPlayer player, ItemStack required) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItem(stack, required)) {
                count += stack.getCount();
                if (count >= required.getCount()) return true;
            }
        }
        return false;
    }

    private static void removeItems(ServerPlayer player, ItemStack required) {
        int toRemove = required.getCount();
        for (ItemStack stack : player.getInventory().items) {
            if (toRemove <= 0) break;
            if (ItemStack.isSameItem(stack, required)) {
                int remove = Math.min(stack.getCount(), toRemove);
                stack.shrink(remove);
                toRemove -= remove;
            }
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}