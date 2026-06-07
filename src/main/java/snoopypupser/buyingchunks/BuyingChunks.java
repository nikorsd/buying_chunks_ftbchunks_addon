package snoopypupser.buyingchunks;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;
import snoopypupser.buyingchunks.claimshop.ClaimShopEventHandler;
import snoopypupser.buyingchunks.claimshop.ClaimShopSync;
import snoopypupser.buyingchunks.command.ClaimShopCommand;
import snoopypupser.buyingchunks.network.BuyingChunksNetwork;

@Mod(BuyingChunks.MOD_ID)
public class BuyingChunks {

    public static final String MOD_ID = "buyingchunks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BuyingChunks(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        new ClaimShopEventHandler().register();
        BuyingChunksNetwork.register(modEventBus);
        LOGGER.info("Buying Chunks is loading...");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Buying Chunks successfully initialized!");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ClaimShopCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            ClaimShopSync.syncToPlayer(player);
        }
    }
}