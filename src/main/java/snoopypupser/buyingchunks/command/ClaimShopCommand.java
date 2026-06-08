package snoopypupser.buyingchunks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import snoopypupser.buyingchunks.claimshop.ClaimShopEntry;
import snoopypupser.buyingchunks.claimshop.ClaimShopSavedData;
import snoopypupser.buyingchunks.claimshop.ClaimShopSync;

import java.util.Optional;

public class ClaimShopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("claimshop")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("set")
                                .then(Commands.argument("item", ResourceArgument.resource(context, Registries.ITEM))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> setForSale(
                                                        ctx.getSource(),
                                                        ResourceArgument.getResource(ctx, "item", Registries.ITEM).value(),
                                                        IntegerArgumentType.getInteger(ctx, "amount")
                                                ))
                                        )
                                )
                        )

                        .then(Commands.literal("remove")
                                .executes(ctx -> removeFromSale(ctx.getSource()))
                        )

                        .then(Commands.literal("setting")
                                .then(Commands.argument("teamname", StringArgumentType.string())
                                        .then(Commands.literal("chunks")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 9999))
                                                        .executes(ctx -> setTeamChunkLimit(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "teamname"),
                                                                IntegerArgumentType.getInteger(ctx, "amount")
                                                        ))
                                                )
                                                .then(Commands.literal("remove")
                                                        .executes(ctx -> removeTeamChunkLimit(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "teamname")
                                                        ))
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("info")
                                .executes(ctx -> getInfo(ctx.getSource()))
                        )

                        .then(Commands.literal("teamprice")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("teamname", StringArgumentType.string())
                                                .then(Commands.argument("item", ResourceArgument.resource(context, Registries.ITEM))
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                                .executes(ctx -> setTeamPrice(
                                                                        ctx.getSource(),
                                                                        StringArgumentType.getString(ctx, "teamname"),
                                                                        ResourceArgument.getResource(ctx, "item", Registries.ITEM).value(),
                                                                        IntegerArgumentType.getInteger(ctx, "amount")
                                                                ))
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("teamname", StringArgumentType.string())
                                                .executes(ctx -> removeTeamPrice(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "teamname")
                                                ))
                                        )
                                )
                        )
        );
    }

    private static int setForSale(CommandSourceStack source, Item item, int amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            ChunkPos chunkPos = new ChunkPos(player.blockPosition());

            ClaimedChunkManager manager = FTBChunksAPI.api().getManager();
            ClaimedChunk claimed = manager.getChunk(new dev.ftb.mods.ftblibrary.math.ChunkDimPos(level.dimension(), chunkPos));
            if (claimed == null) {
                source.sendFailure(Component.translatable("uc7core.claimshop.error.notclaimed"));
                return 0;
            }

            if (!claimed.getTeamData().isTeamMember(player.getUUID()) && !source.hasPermission(4)) {
                source.sendFailure(Component.translatable("uc7core.claimshop.error.notowner"));
                return 0;
            }

            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            String shopTeamName = team.map(t -> t.getName().getString())
                    .orElse(player.getGameProfile().getName());

            ItemStack price = new ItemStack(item, amount);

            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);
            savedData.getData().setForSale(chunkPos, price, shopTeamName, player.getUUID());
            savedData.setDirty();
            ClaimShopSync.syncToAll(player.getServer());

            source.sendSuccess(() -> Component.translatable(
                    "uc7core.claimshop.set.success",
                    chunkPos.x, chunkPos.z, amount,
                    item.getDescription().getString(),
                    shopTeamName
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("uc7core.claimshop.error", e.getMessage()));
            return 0;
        }
    }

    private static int removeFromSale(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            ChunkPos chunkPos = new ChunkPos(player.blockPosition());

            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);
            ClaimShopEntry entry = savedData.getData().getEntry(chunkPos);

            if (entry == null) {
                source.sendFailure(Component.translatable("uc7core.claimshop.info.notforsale", chunkPos.x, chunkPos.z));
                return 0;
            }

            savedData.getData().removeFromSale(chunkPos);
            savedData.setDirty();
            ClaimShopSync.syncToAll(player.getServer());

            source.sendSuccess(() -> Component.translatable(
                    "uc7core.claimshop.remove.success", chunkPos.x, chunkPos.z
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("uc7core.claimshop.error", e.getMessage()));
            return 0;
        }
    }

    private static int getInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            ChunkPos chunkPos = new ChunkPos(player.blockPosition());

            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);
            ClaimShopEntry entry = savedData.getData().getEntry(chunkPos);

            if (entry != null) {
                source.sendSuccess(() -> Component.translatable(
                        "uc7core.claimshop.info.forsale",
                        chunkPos.x, chunkPos.z,
                        entry.getPrice().getCount(),
                        entry.getPrice().getItem().getDescription().getString(),
                        entry.getShopTeamName()
                ), false);
            } else {
                source.sendSuccess(() -> Component.translatable(
                        "uc7core.claimshop.info.notforsale", chunkPos.x, chunkPos.z
                ), false);
            }
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("uc7core.claimshop.error", e.getMessage()));
            return 0;
        }
    }

    private static int setTeamPrice(CommandSourceStack source, String teamName, Item item, int amount) {
        try {
            ServerLevel level = source.getLevel();
            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);

            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeams().stream()
                    .filter(t -> t.getName().getString().equalsIgnoreCase(teamName))
                    .findFirst();

            if (team.isEmpty()) {
                source.sendFailure(Component.translatable("uc7core.claimshop.error.teamnotfound", teamName));
                return 0;
            }

            ItemStack price = new ItemStack(item, amount);
            savedData.getData().setTeamPrice(team.get().getId(), price);
            savedData.setDirty();

            source.sendSuccess(() -> Component.translatable(
                    "uc7core.claimshop.teamprice.set.success",
                    teamName, amount, item.getDescription().getString()
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("uc7core.claimshop.error", e.getMessage()));
            return 0;
        }
    }

    private static int removeTeamPrice(CommandSourceStack source, String teamName) {
        try {
            ServerLevel level = source.getLevel();
            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);

            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeams().stream()
                    .filter(t -> t.getName().getString().equalsIgnoreCase(teamName))
                    .findFirst();

            if (team.isEmpty()) {
                source.sendFailure(Component.translatable("uc7core.claimshop.error.teamnotfound", teamName));
                return 0;
            }

            savedData.getData().removeTeamPrice(team.get().getId());
            savedData.setDirty();

            source.sendSuccess(() -> Component.translatable(
                    "uc7core.claimshop.teamprice.remove.success", teamName
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("uc7core.claimshop.error", e.getMessage()));
            return 0;
        }
    }

    private static int setTeamChunkLimit(CommandSourceStack source, String teamName, int limit) {
        try {
            ServerLevel level = source.getLevel();
            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);

            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeams().stream()
                    .filter(t -> t.getName().getString().equalsIgnoreCase(teamName))
                    .findFirst();

            if (team.isEmpty()) {
                source.sendFailure(Component.translatable("uc7core.claimshop.error.teamnotfound", teamName));
                return 0;
            }

            savedData.getData().setTeamChunkLimit(team.get().getId(), limit);
            savedData.setDirty();

            source.sendSuccess(() -> Component.translatable(
                    "uc7core.claimshop.chunklimit.set.success",
                    teamName, limit
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("uc7core.claimshop.error", e.getMessage()));
            return 0;
        }
    }

    private static int removeTeamChunkLimit(CommandSourceStack source, String teamName) {
        try {
            ServerLevel level = source.getLevel();
            ClaimShopSavedData savedData = ClaimShopSavedData.get(level);

            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeams().stream()
                    .filter(t -> t.getName().getString().equalsIgnoreCase(teamName))
                    .findFirst();

            if (team.isEmpty()) {
                source.sendFailure(Component.translatable("uc7core.claimshop.error.teamnotfound", teamName));
                return 0;
            }

            savedData.getData().removeTeamChunkLimit(team.get().getId());
            savedData.setDirty();

            source.sendSuccess(() -> Component.translatable(
                    "uc7core.claimshop.chunklimit.remove.success", teamName
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("uc7core.claimshop.error", e.getMessage()));
            return 0;
        }
    }
}