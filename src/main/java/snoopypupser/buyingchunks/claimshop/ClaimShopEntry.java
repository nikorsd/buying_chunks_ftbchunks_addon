package snoopypupser.buyingchunks.claimshop;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ClaimShopEntry {
    private final ItemStack price;
    private final String shopTeamName;
    private final UUID sellerUUID;

    public ClaimShopEntry(ItemStack price, String shopTeamName, UUID sellerUUID) {
        this.price = price;
        this.shopTeamName = shopTeamName;
        this.sellerUUID = sellerUUID;
    }

    public ItemStack getPrice() { return price; }
    public String getShopTeamName() { return shopTeamName; }
    public UUID getSellerUUID() { return sellerUUID; }
}