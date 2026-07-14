package com.dagre45.lifestealcore.listeners;

import com.dagre45.lifestealcore.LifestealCore;
import com.dagre45.lifestealcore.items.CustomItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ItemUseListener implements Listener {

    private final LifestealCore plugin;

    public ItemUseListener(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (CustomItems.isReviveBeacon(item)) {
            // Not a placeable/usable block on its own — revival happens via /revive.
            event.setCancelled(true);
            plugin.getMessages().send(player, "revive.needs-beacon");
            return;
        }

        if (CustomItems.isHeartItem(item)) {
            event.setCancelled(true);
            consumeHeartItem(player, item);
        }
    }

    private void consumeHeartItem(Player player, ItemStack item) {
        int maxHearts = plugin.getLsConfig().maxHearts();
        int current = plugin.getHeartManager().hearts(player.getUniqueId());

        if (current >= maxHearts) {
            plugin.getMessages().send(player, "heart-item.at-cap",
                    "maxhearts", String.valueOf(maxHearts));
            return;
        }

        item.setAmount(item.getAmount() - 1);
        plugin.getHeartManager().addHearts(player.getUniqueId(), 1);

        int updated = plugin.getHeartManager().hearts(player.getUniqueId());
        plugin.getMessages().send(player, "heart-item.consumed",
                "hearts", String.valueOf(updated),
                "maxhearts", String.valueOf(maxHearts));
    }
}
