package snoopypupser.buyingchunks.claimshop;

import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientClaimShopData {

    private static Map<ChunkPos, ClaimShopEntry> forSaleChunks = new HashMap<>();

    public static void update(Map<ChunkPos, ClaimShopEntry> data) {
        forSaleChunks = new HashMap<>(data);
        dirty = true;
    }

    public static boolean isForSale(ChunkPos pos) {
        return forSaleChunks.containsKey(pos);
    }

    public static ClaimShopEntry getEntry(ChunkPos pos) {
        return forSaleChunks.get(pos);
    }

    public static Map<ChunkPos, ClaimShopEntry> getAll() {
        return Collections.unmodifiableMap(forSaleChunks);
    }

    private static boolean dirty = false;

    public static void markDirty() {
        dirty = true;
    }

    public static boolean isDirtyAndReset() {
        if (dirty) {
            dirty = false;
            return true;
        }
        return false;
    }
}