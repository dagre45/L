package com.dagre45.lifestealcore;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

/** Typed, cached view over config.yml. Call {@link #reload()} after editing the file. */
public final class LifestealConfig {

    public enum EliminationMode { TEMPORARY_BAN, PERMANENT_BAN, SPECTATOR }
    public enum CombatLogPunishment { KILL, BAN }

    private final LifestealCore plugin;

    private int startingHearts;
    private int maxHearts;
    private int heartsPerKill;
    private boolean loseHeartOnNaturalDeath;
    private int reviveHearts;
    private int withdrawMinHeartsRemaining;

    private EliminationMode eliminationMode;
    private int banDurationHours;
    private boolean broadcastElimination;
    private String broadcastMessage;
    private String banScreenMessage;

    private boolean combatLogEnabled;
    private int combatLogTimerSeconds;
    private CombatLogPunishment combatLogPunishment;
    private String combatLogBroadcast;

    private boolean recipesEnabled;

    public LifestealConfig(LifestealCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        startingHearts = c.getInt("hearts.starting-hearts", 10);
        maxHearts = c.getInt("hearts.max-hearts", 20);
        heartsPerKill = c.getInt("hearts.hearts-per-kill", 1);
        loseHeartOnNaturalDeath = c.getBoolean("hearts.lose-heart-on-natural-death", false);
        reviveHearts = c.getInt("hearts.revive-hearts", 10);
        withdrawMinHeartsRemaining = c.getInt("hearts.withdraw-min-hearts-remaining", 5);

        eliminationMode = EliminationMode.valueOf(c.getString("elimination.mode", "PERMANENT_BAN").toUpperCase());
        banDurationHours = c.getInt("elimination.ban-duration-hours", 72);
        broadcastElimination = c.getBoolean("elimination.broadcast-elimination", true);
        broadcastMessage = c.getString("elimination.broadcast-message", "%player% was eliminated by %killer%!");
        banScreenMessage = c.getString("elimination.ban-screen-message", "You were eliminated.");

        combatLogEnabled = c.getBoolean("combat-log.enabled", true);
        combatLogTimerSeconds = c.getInt("combat-log.timer-seconds", 15);
        combatLogPunishment = CombatLogPunishment.valueOf(c.getString("combat-log.punishment", "KILL").toUpperCase());
        combatLogBroadcast = c.getString("combat-log.broadcast", "%player% combat logged!");

        recipesEnabled = c.getBoolean("recipes.enabled", true);
    }

    public int startingHearts() { return startingHearts; }
    public int maxHearts() { return maxHearts; }
    public int heartsPerKill() { return heartsPerKill; }
    public boolean loseHeartOnNaturalDeath() { return loseHeartOnNaturalDeath; }
    public int reviveHearts() { return reviveHearts; }
    public int withdrawMinHeartsRemaining() { return withdrawMinHeartsRemaining; }

    public EliminationMode eliminationMode() { return eliminationMode; }
    public int banDurationHours() { return banDurationHours; }
    public boolean broadcastElimination() { return broadcastElimination; }
    public String broadcastMessage() { return broadcastMessage; }
    public String banScreenMessage() { return banScreenMessage; }

    public boolean combatLogEnabled() { return combatLogEnabled; }
    public int combatLogTimerSeconds() { return combatLogTimerSeconds; }
    public CombatLogPunishment combatLogPunishment() { return combatLogPunishment; }
    public String combatLogBroadcast() { return combatLogBroadcast; }

    public boolean recipesEnabled() { return recipesEnabled; }

    public List<String> recipeShape(String key) {
        return plugin.getConfig().getStringList("recipes." + key + ".shape");
    }

    public Map<String, Object> recipeIngredients(String key) {
        var section = plugin.getConfig().getConfigurationSection("recipes." + key + ".ingredients");
        return section == null ? Map.of() : section.getValues(false);
    }
}
