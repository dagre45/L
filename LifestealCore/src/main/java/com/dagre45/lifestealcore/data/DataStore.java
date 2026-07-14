package com.dagre45.lifestealcore.data;

import com.dagre45.lifestealcore.LifestealCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Flatfile YAML player-data store. Swap this class for a SQLite/MySQL-backed
 * implementation of the same public methods if you need to scale past a few
 * hundred concurrent players (see docs/INSTALL.md "Storage upgrade").
 */
public final class DataStore {

    private final LifestealCore plugin;
    private final File file;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private volatile boolean dirty = false;

    public DataStore(LifestealCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        var section = yaml.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerData data = new PlayerData(uuid, plugin.getLsConfig().startingHearts());
                data.setHearts(section.getInt(key + ".hearts", plugin.getLsConfig().startingHearts()));
                data.setKills(section.getInt(key + ".kills", 0));
                data.setDeaths(section.getInt(key + ".deaths", 0));
                data.setEliminated(section.getBoolean(key + ".eliminated", false));
                data.setEliminatedAt(section.getLong(key + ".eliminated-at", 0L));
                cache.put(uuid, data);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Skipping malformed playerdata entry: " + key);
            }
        }
    }

    /** Returns cached data, creating a fresh entry with default starting hearts if absent. */
    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> {
            markDirty();
            return new PlayerData(id, plugin.getLsConfig().startingHearts());
        });
    }

    public boolean has(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void markDirty() {
        dirty = true;
    }

    public synchronized void saveAll() {
        if (!dirty) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        for (PlayerData data : cache.values()) {
            String path = "players." + data.getUuid();
            yaml.set(path + ".hearts", data.getHearts());
            yaml.set(path + ".kills", data.getKills());
            yaml.set(path + ".deaths", data.getDeaths());
            yaml.set(path + ".eliminated", data.isEliminated());
            yaml.set(path + ".eliminated-at", data.getEliminatedAt());
        }
        try {
            yaml.save(file);
            dirty = false;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save playerdata.yml", ex);
        }
    }

    public void startAutosave(int intervalMinutes) {
        long ticks = intervalMinutes * 60L * 20L;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveAll, ticks, ticks);
    }
}
