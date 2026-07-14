package com.dagre45.lifestealcore.hooks;

import com.dagre45.lifestealcore.LifestealCore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes: %lifesteal_hearts% %lifesteal_maxhearts% %lifesteal_kills%
 * %lifesteal_deaths% %lifesteal_eliminated%
 * Register economy balance separately via the built-in PlaceholderAPI "Vault" expansion (%vault_eco_balance%).
 */
public final class LifestealPlaceholders extends PlaceholderExpansion {

    private final LifestealCore plugin;

    public LifestealPlaceholders(LifestealCore plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "lifesteal"; }
    @Override public @NotNull String getAuthor() { return "dagre45"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        return switch (params.toLowerCase()) {
            case "hearts" -> String.valueOf(plugin.getHeartManager().hearts(player.getUniqueId()));
            case "maxhearts" -> String.valueOf(plugin.getLsConfig().maxHearts());
            case "kills" -> String.valueOf(plugin.getHeartManager().data(player.getUniqueId()).getKills());
            case "deaths" -> String.valueOf(plugin.getHeartManager().data(player.getUniqueId()).getDeaths());
            case "eliminated" -> plugin.getHeartManager().isEliminated(player.getUniqueId()) ? "true" : "false";
            default -> null;
        };
    }
}
