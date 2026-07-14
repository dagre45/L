package com.dagre45.lifestealcore.items;

import com.dagre45.lifestealcore.LifestealCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Builds and identifies the plugin's two custom items from config.yml.
 * <p>
 * Identification checks the internal PDC tag first, then falls back to a
 * material + custom-model-data match. The fallback matters because items
 * handed out by external plugins (EconomyShopGUI purchases, CrazyCrates
 * rewards) are freshly constructed by those plugins and will not carry our
 * PDC tag — as long as their config points at the same material and
 * custom-model-data as items.heart-item / items.revive-beacon here, they
 * still register as valid heart items / revive beacons.
 */
public final class CustomItems {

    private static LifestealCore plugin;
    private static NamespacedKey heartKey;
    private static NamespacedKey beaconKey;

    private CustomItems() {}

    public static void init(LifestealCore pl) {
        plugin = pl;
        heartKey = new NamespacedKey(plugin, "heart_item");
        beaconKey = new NamespacedKey(plugin, "revive_beacon");
    }

    public static ItemStack createHeartItem(LifestealCore plugin) {
        return build(plugin, "items.heart-item", heartKey);
    }

    public static ItemStack createReviveBeacon(LifestealCore plugin) {
        return build(plugin, "items.revive-beacon", beaconKey);
    }

    private static ItemStack build(LifestealCore plugin, String path, NamespacedKey key) {
        var cfg = plugin.getConfig();
        Material material = Material.matchMaterial(cfg.getString(path + ".material", "NETHER_STAR"));
        if (material == null) {
            material = Material.NETHER_STAR;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                cfg.getString(path + ".display-name", "Item")));

        List<String> lore = cfg.getStringList(path + ".lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .toList();
        meta.setLore(lore);

        int cmd = cfg.getInt(path + ".custom-model-data", 0);
        if (cmd > 0) {
            meta.setCustomModelData(cmd);
        }
        if (cfg.getBoolean(path + ".glow", false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHeartItem(ItemStack item) {
        return hasKey(item, heartKey) || matchesConfigShape(item, "items.heart-item");
    }

    public static boolean isReviveBeacon(ItemStack item) {
        return hasKey(item, beaconKey) || matchesConfigShape(item, "items.revive-beacon");
    }

    private static boolean hasKey(ItemStack item, NamespacedKey key) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta() || key == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    private static boolean matchesConfigShape(ItemStack item, String path) {
        if (plugin == null || item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        var cfg = plugin.getConfig();
        int expectedCmd = cfg.getInt(path + ".custom-model-data", 0);
        if (expectedCmd <= 0) {
            return false; // no reliable fallback signature configured
        }
        Material expectedMaterial = Material.matchMaterial(cfg.getString(path + ".material", ""));
        ItemMeta meta = item.getItemMeta();
        return item.getType() == expectedMaterial
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == expectedCmd;
    }
}
