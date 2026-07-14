package com.dagre45.lifestealcore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Loads messages.yml and formats/sends "%placeholder%"-style strings. */
public final class Messages {

    private final LifestealCore plugin;
    private YamlConfiguration yaml;
    private String prefix;

    public Messages(LifestealCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);

        try (InputStream defStream = plugin.getResource("messages.yml")) {
            if (defStream != null) {
                yaml.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defStream, StandardCharsets.UTF_8)));
            }
        } catch (IOException ignored) {
            // fall back to whatever is on disk
        }
        prefix = raw("prefix");
    }

    private String raw(String path) {
        return yaml.getString(path, "");
    }

    /** key/value pairs are applied as %key% -> value, plus the global "prefix" token and & color codes. */
    public String format(String path, String... kv) {
        String msg = raw(path);
        msg = msg.replace("%prefix%", prefix == null ? "" : prefix);
        for (int i = 0; i + 1 < kv.length; i += 2) {
            msg = msg.replace("%" + kv[i] + "%", kv[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void send(CommandSender target, String path, String... kv) {
        String formatted = format(path, kv);
        if (!formatted.isBlank()) {
            target.sendMessage(formatted);
        }
    }
}
