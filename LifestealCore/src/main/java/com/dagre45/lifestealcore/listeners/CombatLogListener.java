package com.dagre45.lifestealcore.listeners;

import com.dagre45.lifestealcore.LifestealCore;
import com.dagre45.lifestealcore.LifestealConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tags players in PvP combat and punishes them if they log out while tagged. */
public final class CombatLogListener implements Listener {

    private final LifestealCore plugin;
    private final Map<UUID, Long> taggedUntil = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastOpponent = new ConcurrentHashMap<>();

    public CombatLogListener(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getLsConfig().combatLogEnabled()) {
            return;
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        long until = System.currentTimeMillis() + (plugin.getLsConfig().combatLogTimerSeconds() * 1000L);
        boolean freshTag = !taggedUntil.containsKey(victim.getUniqueId());

        taggedUntil.put(victim.getUniqueId(), until);
        taggedUntil.put(attacker.getUniqueId(), until);
        lastOpponent.put(victim.getUniqueId(), attacker.getUniqueId());
        lastOpponent.put(attacker.getUniqueId(), victim.getUniqueId());

        if (freshTag) {
            plugin.getMessages().send(victim, "combat-log.warning",
                    "seconds", String.valueOf(plugin.getLsConfig().combatLogTimerSeconds()));
            plugin.getMessages().send(attacker, "combat-log.warning",
                    "seconds", String.valueOf(plugin.getLsConfig().combatLogTimerSeconds()));
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player p) {
            return p;
        }
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            return p;
        }
        return null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Long until = taggedUntil.remove(player.getUniqueId());
        UUID opponentId = lastOpponent.remove(player.getUniqueId());
        if (until == null || System.currentTimeMillis() > until) {
            return;
        }

        String opponentName = opponentId != null
                ? Bukkit.getOfflinePlayer(opponentId).getName()
                : "the timer";

        if (plugin.getLsConfig().combatLogPunishment() == LifestealConfig.CombatLogPunishment.KILL) {
            if (opponentId != null) {
                plugin.getHeartManager().handlePvpKill(opponentId, player.getUniqueId());
            } else {
                plugin.getHeartManager().removeHearts(player.getUniqueId(), plugin.getLsConfig().heartsPerKill());
            }
        } else {
            plugin.getHeartManager().eliminate(player.getUniqueId(), opponentName);
        }

        String broadcast = ChatColor.translateAlternateColorCodes('&', plugin.getLsConfig().combatLogBroadcast())
                .replace("%player%", player.getName());
        Bukkit.broadcastMessage(broadcast);
    }
}
