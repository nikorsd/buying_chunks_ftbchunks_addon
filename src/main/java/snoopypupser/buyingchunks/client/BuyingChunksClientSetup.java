package snoopypupser.buyingchunks.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import snoopypupser.buyingchunks.BuyingChunks;

@Mod(value = BuyingChunks.MOD_ID, dist = Dist.CLIENT)
public class BuyingChunksClientSetup {

    public BuyingChunksClientSetup(IEventBus modEventBus) {
        new ClaimShopRenderer().register();
    }
}