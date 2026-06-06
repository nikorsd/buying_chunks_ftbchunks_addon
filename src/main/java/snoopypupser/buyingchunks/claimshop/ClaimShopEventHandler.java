package snoopypupser.buyingchunks.claimshop;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.event.ClaimedChunkEvent;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import snoopypupser.buyingchunks.UC7Core;

import java.util.UUID;

public class ClaimShopEventHandler {

    public void register() {
        ClaimedChunkEvent.AFTER_CLAIM.register(this::onChunkClaimed);
        ClaimedChunkEvent.AFTER_UNCLAIM.register(this::onChunkUnclaimed);
    }

    private void onChunkUnclaimed(CommandSourceStack source, ClaimedChunk chunk) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) player.level();
        ClaimShopSavedData savedData = ClaimShopSavedData.get(level);

        ChunkPos pos = chunk.getPos().chunkPos();
        if (!savedData.getData().isForSale(pos)) return;

        savedData.getData().removeFromSale(pos);
        savedData.setDirty();

        player.getServer().execute(() ->
                ClaimShopSync.syncToAll(player.getServer())
        );
    }

    private void onChunkClaimed(CommandSourceStack source, ClaimedChunk chunk) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) player.level();
        ClaimShopSavedData savedData = ClaimShopSavedData.get(level);

        Team claimTeam = chunk.getTeamData().getTeam();
        UUID teamId = claimTeam.getId();

        if (!savedData.getData().hasTeamPrice(teamId)) return;

        ItemStack price = savedData.getData().getTeamPrice(teamId);
        if (price.isEmpty()) return;

        if (claimTeam.isServerTeam()) {
            UC7Core.LOGGER.info("TeamPrice: Server-Team {} claims chunk {} – automatically for sale for {}x {}",
                    claimTeam.getName().getString(),
                    chunk.getPos(),
                    price.getCount(),
                    price.getItem().getDescription().getString()
            );

            savedData.getData().setForSale(
                    chunk.getPos().chunkPos(),
                    price.copy(),
                    claimTeam.getName().getString(),
                    player.getUUID()
            );
            savedData.setDirty();

            // Execute sync delayed on server thread
            player.getServer().execute(() ->
                    ClaimShopSync.syncToAll(player.getServer())
            );
            return;
        }

        if (!hasEnoughItems(player, price)) {
            chunk.getTeamData().unclaim(source, chunk.getPos(), true);
            player.sendSystemMessage(Component.translatable(
                    "uc7core.claimshop.error.notenoughitems",
                    price.getCount(),
                    price.getItem().getDescription().getString()
            ));
            return;
        }

        removeItems(player, price);
        player.sendSystemMessage(Component.translatable(
                "uc7core.claimshop.teamprice.paid",
                price.getCount(),
                price.getItem().getDescription().getString()
        ));
    }

    private boolean hasEnoughItems(ServerPlayer player, ItemStack required) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItem(stack, required)) {
                count += stack.getCount();
                if (count >= required.getCount()) return true;
            }
        }
        return false;
    }

    private void removeItems(ServerPlayer player, ItemStack required) {
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
}