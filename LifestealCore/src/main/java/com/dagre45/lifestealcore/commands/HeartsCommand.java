package com.dagre45.lifestealcore.commands;

import com.dagre45.lifestealcore.LifestealCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HeartsCommand implements CommandExecutor {

    private final LifestealCore plugin;

    public HeartsCommand(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int maxHearts = plugin.getLsConfig().maxHearts();

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                plugin.getMessages().send(sender, "admin.player-only");
                return true;
            }
            int hearts = plugin.getHeartManager().hearts(player.getUniqueId());
            plugin.getMessages().send(player, "hearts.self",
                    "hearts", String.valueOf(hearts), "maxhearts", String.valueOf(maxHearts));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target.getName() == null) {
            plugin.getMessages().send(sender, "revive.player-not-found");
            return true;
        }
        int hearts = plugin.getHeartManager().hearts(target.getUniqueId());
        plugin.getMessages().send(sender, "hearts.other",
                "target", target.getName(), "hearts", String.valueOf(hearts), "maxhearts", String.valueOf(maxHearts));
        return true;
    }
}
