package com.dagre45.lifestealcore.items;

import com.dagre45.lifestealcore.LifestealCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.Map;

/** Registers the two config-driven shaped crafting recipes. */
public final class RecipeManager {

    private final LifestealCore plugin;

    public RecipeManager(LifestealCore plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        if (!plugin.getLsConfig().recipesEnabled()) {
            return;
        }
        register("heart-item", "heart_item", CustomItems.createHeartItem(plugin));
        register("revive-beacon", "revive_beacon", CustomItems.createReviveBeacon(plugin));
    }

    private void register(String configKey, String recipeKeyName, ItemStack result) {
        List<String> shape = plugin.getLsConfig().recipeShape(configKey);
        Map<String, Object> ingredients = plugin.getLsConfig().recipeIngredients(configKey);
        if (shape.isEmpty() || ingredients.isEmpty()) {
            plugin.getLogger().warning("Recipe '" + configKey + "' is missing shape/ingredients in config.yml — skipped.");
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, recipeKeyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape.toArray(new String[0]));

        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            char symbol = entry.getKey().charAt(0);
            String token = String.valueOf(entry.getValue());

            if (token.equalsIgnoreCase("HEART_ITEM")) {
                recipe.setIngredient(symbol, new RecipeChoice.ExactChoice(CustomItems.createHeartItem(plugin)));
                continue;
            }
            Material material = Material.matchMaterial(token);
            if (material == null) {
                plugin.getLogger().warning("Unknown material '" + token + "' in recipe '" + configKey + "'.");
                continue;
            }
            recipe.setIngredient(symbol, material);
        }

        Bukkit.removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }
}
