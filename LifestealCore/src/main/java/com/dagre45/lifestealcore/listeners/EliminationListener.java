package com.dagre45.lifestealcore.listeners;

import com.dagre45.lifestealcore.LifestealCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/** Applies the steal-a-heart mechanic on every PvP kill. */
public final class EliminationListener implements Listener {

    private final LifestealCore plugin;

    public EliminationListener(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.equals(victim)) {
            plugin.getHeartManager().handlePvpKill(killer.getUniqueId(), victim.getUniqueId());
            return;
        }

        if (plugin.getLsConfig().loseHeartOnNaturalDeath()) {
            plugin.getHeartManager().removeHearts(victim.getUniqueId(), 1);
            if (plugin.getHeartManager().hearts(victim.getUniqueId()) <= 0) {
                plugin.getHeartManager().eliminate(victim.getUniqueId(), null);
            }
        }
    }
}
