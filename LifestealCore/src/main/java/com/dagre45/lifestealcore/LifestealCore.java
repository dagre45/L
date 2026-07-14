package com.dagre45.lifestealcore;

import com.dagre45.lifestealcore.commands.AdminCommand;
import com.dagre45.lifestealcore.commands.HeartsCommand;
import com.dagre45.lifestealcore.commands.ReviveCommand;
import com.dagre45.lifestealcore.commands.WithdrawCommand;
import com.dagre45.lifestealcore.data.DataStore;
import com.dagre45.lifestealcore.hooks.LifestealPlaceholders;
import com.dagre45.lifestealcore.items.CustomItems;
import com.dagre45.lifestealcore.items.RecipeManager;
import com.dagre45.lifestealcore.listeners.CombatLogListener;
import com.dagre45.lifestealcore.listeners.EliminationListener;
import com.dagre45.lifestealcore.listeners.ItemUseListener;
import com.dagre45.lifestealcore.listeners.JoinQuitListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class LifestealCore extends JavaPlugin {

    private LifestealConfig lsConfig;
    private Messages messages;
    private DataStore dataStore;
    private HeartManager heartManager;
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.lsConfig = new LifestealConfig(this);
        this.messages = new Messages(this);
        this.dataStore = new DataStore(this);
        this.dataStore.load();
        this.dataStore.startAutosave(getConfig().getInt("storage.autosave-interval-minutes", 5));
        this.heartManager = new HeartManager(this);

        CustomItems.init(this);
        this.recipeManager = new RecipeManager(this);
        this.recipeManager.registerAll();

        getServer().getPluginManager().registerEvents(new EliminationListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatLogListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

        getCommand("hearts").setExecutor(new HeartsCommand(this));
        getCommand("withdrawheart").setExecutor(new WithdrawCommand(this));
        getCommand("revive").setExecutor(new ReviveCommand(this));
        getCommand("lifesteal").setExecutor(new AdminCommand(this));

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LifestealPlaceholders(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        getLogger().info("LifestealCore enabled.");
    }

    @Override
    public void onDisable() {
        if (dataStore != null) {
            dataStore.saveAll();
        }
    }

    public LifestealConfig getLsConfig() { return lsConfig; }
    public Messages getMessages() { return messages; }
    public DataStore getDataStore() { return dataStore; }
    public HeartManager getHeartManager() { return heartManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
}
