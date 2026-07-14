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

/** /lifesteal give|sethearts|ban|unban|reload — see plugin.yml for permission. */
public final class AdminCommand implements CommandExecutor {

    private final LifestealCore plugin;

    public AdminCommand(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.getMessages().send(sender, "admin.invalid-usage",
                    "usage", "/lifesteal <give|sethearts|ban|unban|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> give(sender, args);
            case "sethearts" -> setHearts(sender, args);
            case "ban" -> ban(sender, args);
            case "unban" -> unban(sender, args);
            case "reload" -> reload(sender);
            default -> plugin.getMessages().send(sender, "admin.invalid-usage",
                    "usage", "/lifesteal <give|sethearts|ban|unban|reload>");
        }
        return true;
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessages().send(sender, "admin.invalid-usage", "usage", "/lifesteal give <player> <heart|revivebeacon> [amount]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessages().send(sender, "revive.player-not-found");
            return;
        }
        int amount = args.length >= 4 ? parseIntOrDefault(args[3], 1) : 1;

        ItemStack item = switch (args[2].toLowerCase()) {
            case "heart" -> CustomItems.createHeartItem(plugin);
            case "revivebeacon" -> CustomItems.createReviveBeacon(plugin);
            default -> null;
        };
        if (item == null) {
            plugin.getMessages().send(sender, "admin.invalid-usage", "usage", "/lifesteal give <player> <heart|revivebeacon> [amount]");
            return;
        }
        item.setAmount(Math.max(1, amount));
        target.getInventory().addItem(item);
        plugin.getMessages().send(sender, "admin.gave-item",
                "amount", String.valueOf(amount), "item", args[2].toLowerCase(), "target", target.getName());
    }

    private void setHearts(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessages().send(sender, "admin.invalid-usage", "usage", "/lifesteal sethearts <player> <amount>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            plugin.getMessages().send(sender, "revive.player-not-found");
            return;
        }
        int amount = parseIntOrDefault(args[2], -1);
        if (amount < 0) {
            plugin.getMessages().send(sender, "admin.invalid-usage", "usage", "/lifesteal sethearts <player> <amount>");
            return;
        }
        plugin.getHeartManager().setHearts(target.getUniqueId(), amount);
        plugin.getMessages().send(sender, "admin.set-hearts", "target", target.getName(), "hearts", String.valueOf(amount));
    }

    private void ban(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "admin.invalid-usage", "usage", "/lifesteal ban <player>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            plugin.getMessages().send(sender, "revive.player-not-found");
            return;
        }
        plugin.getHeartManager().eliminate(target.getUniqueId(), sender.getName());
        plugin.getMessages().send(sender, "admin.banned-player", "target", target.getName());
    }

    private void unban(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "admin.invalid-usage", "usage", "/lifesteal unban <player>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null) {
            plugin.getMessages().send(sender, "revive.player-not-found");
            return;
        }
        plugin.getHeartManager().revive(target.getUniqueId());
        int hearts = plugin.getHeartManager().hearts(target.getUniqueId());
        plugin.getMessages().send(sender, "admin.unbanned", "target", target.getName(), "hearts", String.valueOf(hearts));
    }

    private void reload(CommandSender sender) {
        plugin.getLsConfig().reload();
        plugin.getMessages().load();
        plugin.getRecipeManager().registerAll();
        plugin.getMessages().send(sender, "admin.reload-success");
    }

    private int parseIntOrDefault(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return def;
        }
    }
}
