package snoopypupser.buyingchunks.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import snoopypupser.buyingchunks.BuyingChunks;

public class BuyingChunksNetwork {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(BuyingChunksNetwork::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(BuyingChunks.MOD_ID).versioned("1.0.0");
        registrar.playToClient(
                SyncClaimShopPacket.TYPE,
                SyncClaimShopPacket.STREAM_CODEC,
                SyncClaimShopPacket::handle
        );
        registrar.playToServer(
                BuyChunkPacket.TYPE,
                BuyChunkPacket.STREAM_CODEC,
                BuyChunkPacket::handle
        );
    }
}