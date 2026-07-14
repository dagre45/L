package com.dagre45.lifestealcore.listeners;

import com.dagre45.lifestealcore.LifestealCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class JoinQuitListener implements Listener {

    private final LifestealCore plugin;

    public JoinQuitListener(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        // Ensures playerdata exists (creates with starting-hearts on first join).
        plugin.getHeartManager().data(player.getUniqueId());
        // If they were TEMPORARY_BAN'd and the ban naturally expired, clear the stale flag + restore hearts.
        plugin.getHeartManager().resolveExpiredTemporaryBan(player);
        plugin.getHeartManager().applyToEntity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        // Bukkit resets current health to max on respawn; make sure "max" reflects this player's hearts.
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getHeartManager().applyToEntity(event.getPlayer()));
    }
}
