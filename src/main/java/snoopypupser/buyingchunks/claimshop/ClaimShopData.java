package snoopypupser.buyingchunks.claimshop;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimShopData {

    private final Map<ChunkPos, ClaimShopEntry> forSaleChunks = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<UUID, ItemStack> teamPrices = new HashMap<>();

    // --- Chunk Shop ---

    public void setForSale(ChunkPos pos, ItemStack price, String shopTeamName, UUID sellerUUID) {
        forSaleChunks.put(pos, new ClaimShopEntry(price.copy(), shopTeamName, sellerUUID));
    }

    public void removeFromSale(ChunkPos pos) {
        forSaleChunks.remove(pos);
    }

    public boolean isForSale(ChunkPos pos) {
        return forSaleChunks.containsKey(pos);
    }

    public ClaimShopEntry getEntry(ChunkPos pos) {
        return forSaleChunks.get(pos);
    }

    public Map<ChunkPos, ClaimShopEntry> getAllForSaleMap() {
        return Collections.unmodifiableMap(forSaleChunks);
    }

    // --- Team Prices ---

    public void setTeamPrice(UUID teamId, ItemStack price) {
        teamPrices.put(teamId, price.copy());
    }

    public void removeTeamPrice(UUID teamId) {
        teamPrices.remove(teamId);
    }

    public boolean hasTeamPrice(UUID teamId) {
        return teamPrices.containsKey(teamId);
    }

    public ItemStack getTeamPrice(UUID teamId) {
        return teamPrices.getOrDefault(teamId, ItemStack.EMPTY);
    }

    public Map<UUID, ItemStack> getAllTeamPrices() {
        return Collections.unmodifiableMap(teamPrices);
    }

    // --- NBT Save/Load ---

    private static CompoundTag saveItemStack(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        tag.putInt("count", stack.getCount());
        return tag;
    }

    private static ItemStack loadItemStack(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
        Item item = BuiltInRegistries.ITEM.get(id);
        int count = tag.getInt("count");
        return new ItemStack(item, count);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ListTag chunkList = new ListTag();
        for (Map.Entry<ChunkPos, ClaimShopEntry> entry : forSaleChunks.entrySet()) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("x", entry.getKey().x);
            chunkTag.putInt("z", entry.getKey().z);
            chunkTag.put("price", saveItemStack(entry.getValue().getPrice()));
            chunkTag.putString("shopTeamName", entry.getValue().getShopTeamName());
            chunkTag.putUUID("sellerUUID", entry.getValue().getSellerUUID());
            chunkList.add(chunkTag);
        }
        tag.put("chunks", chunkList);

        ListTag teamList = new ListTag();
        for (Map.Entry<UUID, ItemStack> entry : teamPrices.entrySet()) {
            CompoundTag teamTag = new CompoundTag();
            teamTag.putUUID("teamId", entry.getKey());
            teamTag.put("price", saveItemStack(entry.getValue()));
            teamList.add(teamTag);
        }
        tag.put("teamPrices", teamList);

        return tag;
    }

    public void load(CompoundTag tag) {
        forSaleChunks.clear();
        teamPrices.clear();

        ListTag chunkList = tag.getList("chunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < chunkList.size(); i++) {
            CompoundTag chunkTag = chunkList.getCompound(i);
            int x = chunkTag.getInt("x");
            int z = chunkTag.getInt("z");
            ItemStack price = loadItemStack(chunkTag.getCompound("price"));
            String shopTeamName = chunkTag.getString("shopTeamName");
            UUID sellerUUID = chunkTag.getUUID("sellerUUID");
            forSaleChunks.put(new ChunkPos(x, z), new ClaimShopEntry(price, shopTeamName, sellerUUID));
        }

        ListTag teamList = tag.getList("teamPrices", Tag.TAG_COMPOUND);
        for (int i = 0; i < teamList.size(); i++) {
            CompoundTag teamTag = teamList.getCompound(i);
            UUID teamId = teamTag.getUUID("teamId");
            ItemStack price = loadItemStack(teamTag.getCompound("price"));
            teamPrices.put(teamId, price);
        }
    }
}