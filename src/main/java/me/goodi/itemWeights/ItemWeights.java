package me.goodi.itemWeights;

import me.goodi.itemWeights.commands.reloadConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ItemWeights extends JavaPlugin {

    private Config config;
    private WeightManager weightManager;

    @Override
    public void onEnable() {
        config = new Config();
        weightManager = new WeightManager(this, config);
        File file = new File(getDataFolder() + "/config.yml");
        config.setup(file, getLogger(), weightManager);

        weightManager.initialize();

        getCommand("reloadconfig").setExecutor(new reloadConfig(config));

        getServer().getPluginManager().registerEvents(new WeightManager(this, config),this);


    }

    @Override
    public void onDisable() {
        if(config != null) config.save();
    }
}
