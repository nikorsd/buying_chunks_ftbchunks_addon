package snoopypupser.buyingchunks.client;

import dev.ftb.mods.ftbchunks.api.client.event.MapIconEvent;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import snoopypupser.buyingchunks.claimshop.ClientClaimShopData;
import snoopypupser.buyingchunks.claimshop.ClaimShopEntry;
import snoopypupser.buyingchunks.network.BuyChunkPacket;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClaimShopRenderer {

    private static final dev.ftb.mods.ftblibrary.icon.Icon SHOP_ICON =
            Icons.MONEY.withColor(Color4I.rgb(0xFFD700));

    private static final int COLOR_GOLD   = 0xFFD700;
    private static final int COLOR_GREEN  = 0x55FF55;
    private static final int COLOR_GRAY   = 0xAAAAAA;
    private static final int COLOR_WHITE  = 0xFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFF55;

    public void register() {
        MapIconEvent.LARGE_MAP.register(this::onLargeMapIcons);
    }

    private void onLargeMapIcons(MapIconEvent event) {
        for (Map.Entry<ChunkPos, ClaimShopEntry> entry : ClientClaimShopData.getAll().entrySet()) {
            ChunkPos pos = entry.getKey();
            ClaimShopEntry shopEntry = entry.getValue();

            Vec3 iconPos = new Vec3((pos.x + 0.5) * 16.0, 64, (pos.z + 0.5) * 16.0);

            final int chunkX = pos.x;
            final int chunkZ = pos.z;

            event.add(new MapIcon.SimpleMapIcon(iconPos, SHOP_ICON) {
                @Override
                public void addTooltip(TooltipList list) {
                    // Title
                    list.add(Component.literal("⭐ ")
                            .withStyle(Style.EMPTY.withColor(COLOR_GOLD))
                            .append(Component.translatable("uc7core.claimshop.tooltip.title")
                                    .withStyle(Style.EMPTY.withColor(COLOR_GOLD).withBold(true))));

                    // Divider
                    list.add(Component.translatable("uc7core.claimshop.tooltip.divider")
                            .withStyle(Style.EMPTY.withColor(COLOR_GRAY)));

                    // Chunk position
                    list.add(Component.literal("📍 ")
                            .withStyle(Style.EMPTY.withColor(COLOR_GRAY))
                            .append(Component.translatable("uc7core.claimshop.tooltip.position",
                                    Component.literal(chunkX + ", " + chunkZ)
                                            .withStyle(Style.EMPTY.withColor(COLOR_GRAY)))));

                    // Price
                    list.add(Component.literal("💰 ")
                            .withStyle(Style.EMPTY.withColor(COLOR_GREEN))
                            .append(Component.translatable("uc7core.claimshop.tooltip.price",
                                    Component.literal(shopEntry.getPrice().getCount() + "x ")
                                            .withStyle(Style.EMPTY.withColor(COLOR_GREEN).withBold(true)),
                                    Component.literal(shopEntry.getPrice().getItem().getDescription().getString())
                                            .withStyle(Style.EMPTY.withColor(COLOR_GREEN)))));

                    // Divider
                    list.add(Component.translatable("uc7core.claimshop.tooltip.divider")
                            .withStyle(Style.EMPTY.withColor(COLOR_GRAY)));

                    // Click hint
                    list.add(Component.literal("[ ")
                            .withStyle(Style.EMPTY.withColor(COLOR_GRAY))
                            .append(Component.translatable("uc7core.claimshop.tooltip.click")
                                    .withStyle(Style.EMPTY.withColor(COLOR_YELLOW).withBold(true)))
                            .append(Component.literal(" ]")
                                    .withStyle(Style.EMPTY.withColor(COLOR_GRAY))));
                }

                @Override
                public boolean onMousePressed(BaseScreen screen, MouseButton button) {
                    if (button.isLeft()) {
                        PacketDistributor.sendToServer(new BuyChunkPacket(chunkX, chunkZ));
                        return true;
                    }
                    return false;
                }

                @Override
                public int getPriority() {
                    return 100;
                }
            });
        }
    }
}