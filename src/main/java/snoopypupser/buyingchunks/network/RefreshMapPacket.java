package snoopypupser.buyingchunks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import snoopypupser.buyingchunks.BuyingChunks;
import snoopypupser.buyingchunks.claimshop.ClientClaimShopData;

public record RefreshMapPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RefreshMapPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BuyingChunks.MOD_ID, "refresh_map"));

    public static final StreamCodec<FriendlyByteBuf, RefreshMapPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {}, buf -> new RefreshMapPacket());

    public static void handle(RefreshMapPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientClaimShopData.markDirty());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}