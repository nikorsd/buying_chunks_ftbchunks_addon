package snoopypupser.buyingchunks.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import snoopypupser.buyingchunks.UC7Core;

@Mod(value = UC7Core.MOD_ID, dist = Dist.CLIENT)
public class UC7ClientSetup {

    public UC7ClientSetup(IEventBus modEventBus) {
        new ClaimShopRenderer().register();
    }
}