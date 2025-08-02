package me.goodi.itemWeights;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

public class Config {

    private YamlConfiguration yml;
    private File file;
    private Logger logger;
    private WeightManager weightManager;

    public final double DEFAULT_WEIGHT = 0.0002;

    public void setup(File configFile, Logger logger, WeightManager weightManager) {
        this.file = configFile;
        this.logger = logger;
        this.weightManager = weightManager;

        if (!file.exists()) {
            file.mkdir();
        }

        yml = YamlConfiguration.loadConfiguration(configFile);

        yml.addDefault("WeightsEnabled", true);
        addDefaultWeights();
        yml.options().copyDefaults(true);


        save();
    }

    public void save() {
        try {
            yml.save(file);
        } catch (IOException e) {
            logger.warning("Failed to save configuration to " + file.getPath());
        }
    }

    public YamlConfiguration getConfig() {
        return yml;
    }

    public void reload() {
        yml = YamlConfiguration.loadConfiguration(file);
        weightManager.preloadItemWeights();
    }

    public double getItemWeight(Material material) {
        return getItemWeight(material.name());
    }

    public double getItemWeight(String name) {

        String normalized = name.trim().toUpperCase(Locale.ROOT);


        return getConfig().getDouble("items." + normalized, DEFAULT_WEIGHT);
    }

    public void addDefaultWeights(){

        for(Material material : Material.values()){
            if(!material.isItem()) continue;

            getConfig().addDefault("items." + material.name(), DEFAULT_WEIGHT);
        }
    }

    public boolean isWeightActive(){
        return getConfig().getBoolean("WeightsEnabled", true);
    }

}

