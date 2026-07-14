package com.dagre45.lifestealcore.commands;

import com.dagre45.lifestealcore.LifestealCore;
import com.dagre45.lifestealcore.items.CustomItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class WithdrawCommand implements CommandExecutor {

    private final LifestealCore plugin;

    public WithdrawCommand(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().send(sender, "admin.player-only");
            return true;
        }
        if (!player.hasPermission("lifesteal.withdraw")) {
            plugin.getMessages().send(player, "admin.no-permission");
            return true;
        }

        int hearts = plugin.getHeartManager().hearts(player.getUniqueId());
        int minRemaining = plugin.getLsConfig().withdrawMinHeartsRemaining();

        if (hearts - 1 < minRemaining) {
            plugin.getMessages().send(player, "withdraw.too-low", "minremaining", String.valueOf(minRemaining));
            return true;
        }

        ItemStack heartItem = CustomItems.createHeartItem(plugin);
        if (player.getInventory().firstEmpty() == -1) {
            plugin.getMessages().send(player, "withdraw.inventory-full");
            return true;
        }

        plugin.getHeartManager().removeHearts(player.getUniqueId(), 1);
        player.getInventory().addItem(heartItem);

        int remaining = plugin.getHeartManager().hearts(player.getUniqueId());
        plugin.getMessages().send(player, "withdraw.success", "hearts", String.valueOf(remaining));
        return true;
    }
}
