package snoopypupser.buyingchunks.claimshop;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ClaimShopSavedData extends SavedData {

    private static final String DATA_NAME = "uc7_claimshop";
    private final ClaimShopData data = new ClaimShopData();

    public static ClaimShopSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        ClaimShopSavedData::new,
                        ClaimShopSavedData::load
                ),
                DATA_NAME
        );
    }

    public ClaimShopData getData() {
        return data;
    }

    private static ClaimShopSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ClaimShopSavedData savedData = new ClaimShopSavedData();
        savedData.data.load(tag);
        return savedData;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.merge(data.save());
        return tag;
    }
}