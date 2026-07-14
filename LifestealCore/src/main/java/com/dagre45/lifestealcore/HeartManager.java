package com.dagre45.lifestealcore;

import com.dagre45.lifestealcore.data.PlayerData;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

/**
 * Owns all heart-count mutation and the resulting elimination/ban side effects.
 * "Hearts" here means the UI heart count (1 heart = 2 HP); the Bukkit max-health
 * attribute is derived from it via {@link #applyToEntity(Player)}.
 */
public final class HeartManager {

    private static final double MIN_HEALTH_FLOOR = 1.0D; // never let the attribute hit 0/negative

    private final LifestealCore plugin;

    public HeartManager(LifestealCore plugin) {
        this.plugin = plugin;
    }

    public PlayerData data(UUID uuid) {
        return plugin.getDataStore().get(uuid);
    }

    public int hearts(UUID uuid) {
        return data(uuid).getHearts();
    }

    public boolean isEliminated(UUID uuid) {
        return data(uuid).isEliminated();
    }

    /** Pushes a player's stored heart count onto their live max-health attribute + current health. */
    public void applyToEntity(Player player) {
        int hearts = hearts(player.getUniqueId());
        double maxHealth = Math.max(hearts * 2.0D, MIN_HEALTH_FLOOR);

        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance == null) {
            return;
        }
        instance.setBaseValue(maxHealth);
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        } else if (player.getHealth() <= 0) {
            player.setHealth(Math.min(maxHealth, 1.0D));
        }
    }

    public void setHearts(UUID uuid, int hearts) {
        int clamped = Math.max(0, Math.min(hearts, plugin.getLsConfig().maxHearts()));
        PlayerData d = data(uuid);
        d.setHearts(clamped);
        plugin.getDataStore().markDirty();
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            applyToEntity(online);
        }
    }

    public void addHearts(UUID uuid, int amount) {
        setHearts(uuid, hearts(uuid) + amount);
    }

    public void removeHearts(UUID uuid, int amount) {
        setHearts(uuid, hearts(uuid) - amount);
    }

    /** Core steal mechanic: called from the death listener and from combat-log punishment. */
    public void handlePvpKill(UUID killerId, UUID victimId) {
        int loss = plugin.getLsConfig().heartsPerKill();

        PlayerData victim = data(victimId);
        int newVictimHearts = Math.max(0, victim.getHearts() - loss);
        victim.setHearts(newVictimHearts);
        victim.setDeaths(victim.getDeaths() + 1);

        PlayerData killer = data(killerId);
        int newKillerHearts = Math.min(plugin.getLsConfig().maxHearts(), killer.getHearts() + loss);
        killer.setHearts(newKillerHearts);
        killer.setKills(killer.getKills() + 1);

        plugin.getDataStore().markDirty();

        Player killerPlayer = Bukkit.getPlayer(killerId);
        if (killerPlayer != null) {
            applyToEntity(killerPlayer);
            plugin.getMessages().send(killerPlayer, "hearts.gained-kill",
                    "victim", Bukkit.getOfflinePlayer(victimId).getName(),
                    "hearts", String.valueOf(newKillerHearts),
                    "maxhearts", String.valueOf(plugin.getLsConfig().maxHearts()));
        }

        Player victimPlayer = Bukkit.getPlayer(victimId);
        if (victimPlayer != null) {
            plugin.getMessages().send(victimPlayer, "hearts.lost-kill",
                    "killer", Bukkit.getOfflinePlayer(killerId).getName(),
                    "hearts", String.valueOf(newVictimHearts),
                    "maxhearts", String.valueOf(plugin.getLsConfig().maxHearts()));
        }

        if (newVictimHearts <= 0) {
            eliminate(victimId, Bukkit.getOfflinePlayer(killerId).getName());
        }
    }

    public void eliminate(UUID uuid, String killerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
        if (target.getName() == null) {
            return;
        }
        if (target.isOnline() && target.getPlayer() != null
                && target.getPlayer().hasPermission("lifesteal.bypass.elimination")) {
            return;
        }

        PlayerData d = data(uuid);
        d.setEliminated(true);
        d.setEliminatedAt(System.currentTimeMillis());
        plugin.getDataStore().markDirty();

        LifestealConfig.EliminationMode mode = plugin.getLsConfig().eliminationMode();
        String reason = ChatColor.translateAlternateColorCodes('&', plugin.getLsConfig().banScreenMessage())
                .replace("%player%", target.getName());

        switch (mode) {
            case TEMPORARY_BAN -> {
                Date expiry = new Date(System.currentTimeMillis() + plugin.getLsConfig().banDurationHours() * 3_600_000L);
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, expiry, "LifestealCore");
                kickIfOnline(target, reason);
            }
            case PERMANENT_BAN -> {
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, "LifestealCore");
                kickIfOnline(target, reason);
            }
            case SPECTATOR -> {
                Player online = target.getPlayer();
                if (online != null) {
                    online.setGameMode(GameMode.SPECTATOR);
                    plugin.getMessages().send(online, "elimination.banned");
                }
            }
        }

        if (plugin.getLsConfig().broadcastElimination()) {
            String msg = ChatColor.translateAlternateColorCodes('&', plugin.getLsConfig().broadcastMessage())
                    .replace("%player%", target.getName())
                    .replace("%killer%", killerName == null ? "the void" : killerName);
            Bukkit.broadcastMessage(msg);
        }
    }

    private void kickIfOnline(OfflinePlayer target, String reason) {
        Player online = target.getPlayer();
        if (online != null) {
            online.kickPlayer(reason);
        }
    }

    /** Un-bans/un-spectates and restores hearts to the configured revive amount. */
    public void revive(UUID uuid) {
        PlayerData d = data(uuid);
        d.setEliminated(false);
        d.setHearts(plugin.getLsConfig().reviveHearts());
        plugin.getDataStore().markDirty();

        OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
        if (target.getName() != null) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(target.getName());
        }

        Player online = target.getPlayer();
        if (online != null) {
            if (online.getGameMode() == GameMode.SPECTATOR) {
                online.setGameMode(GameMode.SURVIVAL);
            }
            applyToEntity(online);
        }
    }

    /** Called on join in case a TEMPORARY_BAN naturally expired — clears the stale eliminated flag. */
    public void resolveExpiredTemporaryBan(Player player) {
        PlayerData d = data(player.getUniqueId());
        if (d.isEliminated() && plugin.getLsConfig().eliminationMode() == LifestealConfig.EliminationMode.TEMPORARY_BAN) {
            d.setEliminated(false);
            d.setHearts(plugin.getLsConfig().reviveHearts());
            plugin.getDataStore().markDirty();
        }
    }
}
