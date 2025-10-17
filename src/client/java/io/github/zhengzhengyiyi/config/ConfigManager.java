package io.github.zhengzhengyiyi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .create();
    
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("renderboost.json");
    private static RenderBoostConfig config;
    
    public static RenderBoostConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }
    
    public static void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            config = new RenderBoostConfig();
            saveConfig();
            return;
        }
        
        try {
            String json = Files.readString(CONFIG_PATH);
            config = GSON.fromJson(json, RenderBoostConfig.class);
            if (config == null) {
                config = new RenderBoostConfig();
                saveConfig();
            }
        } catch (IOException e) {
            System.err.println("can not load file, use default" + e.getMessage());
            config = new RenderBoostConfig();
            saveConfig();
        }
    }
    
    public static void saveConfig() {
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("failed to save file: " + e.getMessage());
        }
    }
    
    public static void resetToDefaults() {
        config = new RenderBoostConfig();
        saveConfig();
    }
}
