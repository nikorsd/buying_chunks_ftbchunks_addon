# 🏪 Buying Chunks — FTB Chunks Addon

A NeoForge 1.21.1 addon for FTB Chunks that adds a fully-featured **chunk shop system** to your server. Admins can put claimed chunks up for sale, and players can browse and purchase them directly from the FTB Chunks map GUI.

---

## ✨ Features

- **Chunk Shop** — Admins can list claimed chunks for sale with a custom item price
- **Visual Map Icons** — For-sale chunks appear as golden icons on the FTB Chunks large map
- **Hover Tooltips** — Hover over a chunk icon to see the price and buy prompt
- **One-Click Purchase** — Left-click a shop icon on the map to instantly buy the chunk
- **Team Shop System** — Create server teams with a fixed price per chunk; any chunk claimed by that team is automatically listed for sale
- **Automatic Sync** — All clients are updated in real-time when chunks are listed or purchased
- **Sound Feedback** — Distinct sounds for successful purchases and error states
- **Multilingual** — Fully translatable via https://crowdin.com/project/buying-chunks! (more languages can be added)
- **OP-Only Commands** — All admin commands require OP Level 2 or higher

---

## 📦 Dependencies

| Mod | Version |
|-----|---------|
| NeoForge | 21.1.224 |
| FTB Chunks (NeoForge) | 2101.1.14+ |
| FTB Teams (NeoForge) | 2101.1.9+ |
| FTB Library (NeoForge) | 2101.1.30+ |
| Architectury (NeoForge) | 13.0.8+ |

All dependencies must be installed on **both the server and every client**.

---

## 🚀 Installation

1. Download `buyingchunks-1.0.0.jar`
2. Place it in the `mods/` folder of your server **and** all clients
3. Make sure all dependencies listed above are also installed
4. Start the server — no additional configuration required

---

## 🛠️ Admin Setup Guide

### Setting Up a Shop District (Recommended)

This is the easiest way to create a purchasable area on your server:

```
# 1. Create a server team for your shop district
/ftbteams server create Shopping-District

# 2. Set a fixed price for all chunks claimed by this team
/claimshop teamprice set "Shopping-District" minecraft:diamond 5

# 3. Open the FTB Chunks claim GUI as the team and claim your chunks
/ftbchunks admin open_claim_gui_as Shopping-District#<team-id>
```

Any chunk claimed by the `Shopping-District` team is **automatically listed for sale** at the configured price. Players can then purchase these chunks directly from the map.

---

### Manually Listing a Chunk for Sale

Stand in the chunk you want to sell and run:

```
/claimshop set <item> <amount>
```

**Example:**
```
/claimshop set minecraft:diamond 5
```

The chunk must already be claimed before it can be listed.

---

### Removing a Chunk from Sale

Stand in the chunk and run:

```
/claimshop remove
```

Unclaiming a chunk also automatically removes it from the shop.

---

### Checking Chunk Info

Stand in any chunk and run:

```
/claimshop info
```

---

### Team Price Management

Set a fixed price for all chunks claimed by a specific team:

```
/claimshop teamprice set <teamname> <item> <amount>
/claimshop teamprice remove <teamname>
```

**Examples:**
```
/claimshop teamprice set "Shopping-District" minecraft:diamond 5
/claimshop teamprice remove "Shopping-District"
```

---

## 🛒 Player Buying Guide

1. Open the **FTB Chunks large map** (default: `M`)
2. Look for **golden coin icons** — these are chunks for sale
3. **Hover** over an icon to see the price
4. **Left-click** the icon to purchase the chunk
5. Make sure you have the required items in your inventory!

The chunk will be transferred to your team immediately upon purchase.

---

## 💬 Commands Reference

| Command | Description | Permission |
|---------|-------------|------------|
| `/claimshop set <item> <amount>` | List the current chunk for sale | OP 2+ |
| `/claimshop remove` | Remove the current chunk from sale | OP 2+ |
| `/claimshop info` | Show shop info for the current chunk | OP 2+ |
| `/claimshop teamprice set <team> <item> <amount>` | Set a fixed price for a team | OP 2+ |
| `/claimshop teamprice remove <team>` | Remove a team's fixed price | OP 2+ |

---

## 🌍 Translations

The mod supports full translation. 
It can be translated even more here: https://crowdin.com/project/buying-chunks

---

## ⚙️ How It Works

- Shop data is saved server-side using Minecraft's `SavedData` system — it persists across restarts
- When a player joins, all current shop listings are synced to their client
- When a chunk is purchased, the old claim is removed and the chunk is re-claimed under the buyer's team
- Items are taken directly from the buyer's inventory

---

## 🐛 Known Limitations

- Chunks can only be purchased in the **Overworld** dimension (where the player is located)
- Players must be in a **non-server team** to purchase chunks
- Large bulk claims (100+ chunks at once) may cause brief lag during sync

---

## 📄 License

All Rights Reserved © snoopypupser
