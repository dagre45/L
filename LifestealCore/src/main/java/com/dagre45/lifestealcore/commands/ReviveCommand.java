package com.dagre45.lifestealcore.commands;

import com.dagre45.lifestealcore.LifestealCore;
import com.dagre45.lifestealcore.items.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ReviveCommand implements CommandExecutor {

    private final LifestealCore plugin;

    public ReviveCommand(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().send(sender, "admin.player-only");
            return true;
        }
        if (args.length != 1) {
            plugin.getMessages().send(player, "admin.invalid-usage", "usage", "/revive <player>");
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!CustomItems.isReviveBeacon(held)) {
            plugin.getMessages().send(player, "revive.needs-beacon");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target.getName() == null) {
            plugin.getMessages().send(player, "revive.player-not-found");
            return true;
        }
        if (!plugin.getHeartManager().isEliminated(target.getUniqueId())) {
            plugin.getMessages().send(player, "revive.not-eliminated", "target", target.getName());
            return true;
        }

        held.setAmount(held.getAmount() - 1);
        plugin.getHeartManager().revive(target.getUniqueId());

        int hearts = plugin.getHeartManager().hearts(target.getUniqueId());
        plugin.getMessages().send(player, "revive.success-reviver", "target", target.getName(), "hearts", String.valueOf(hearts));

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            plugin.getMessages().send(onlineTarget, "revive.success-target", "reviver", player.getName(), "hearts", String.valueOf(hearts));
        }

        String broadcast = plugin.getMessages().format("revive.broadcast", "target", target.getName(), "reviver", player.getName());
        Bukkit.broadcastMessage(broadcast);
        return true;
    }
}
