package com.nametagedit.plugin;

import com.nametagedit.plugin.listener.NametagListener;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.flatfile.FlatFileConfig;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class NametagEdit extends JavaPlugin {

    private AbstractConfig config;
    private NametagHandler handler;
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        // Setup LuckPerms API
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("Successfully hooked into LuckPerms.");
        }

        // Initialize handler and storage
        handler = new NametagHandler(this);
        config = new FlatFileConfig(this, handler);
        handler.setStorage(config);
        config.load();

        // Register commands and listeners
        getCommand("ne").setExecutor(new NametagCommand(handler));
        getServer().getPluginManager().registerEvents(new NametagListener(handler), this);

        // Apply tags to already online players (for reloads)
        handler.applyTags();

        getLogger().info("NametagEdit has been enabled.");
    }

    @Override
    public void onDisable() {
        if (config != null) {
            config.shutdown();
        }
        getLogger().info("NametagEdit has been disabled.");
    }

    public NametagHandler getHandler() {
        return handler;
    }

    public AbstractConfig getStorage() {
        return config;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}