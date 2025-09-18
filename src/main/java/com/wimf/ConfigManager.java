package com.wimf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigManager {
    private static final ConfigManager INSTANCE = new ConfigManager();
    public static ConfigManager getInstance() { return INSTANCE; }
    private ConfigManager() {}

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configFile = FabricLoader.getInstance().getConfigDir().resolve("wimf_config.json");

    // Приватное поле для хранения загруженной конфигурации
    private ModConfig config = new ModConfig();

    // Публичный геттер, чтобы все могли получить доступ к настройкам
    public ModConfig getConfig() {
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            GSON.toJson(this.config, writer); // Сохраняем весь объект config
        } catch (IOException e) {
            System.err.println("[WIMF] Не удалось сохранить файл конфигурации!");
            e.printStackTrace();
        }
    }

    public void load() {
        if (!configFile.toFile().exists()) {
            // Если файла нет, просто сохраняем конфиг по умолчанию
            save();
            return;
        }

        try (FileReader reader = new FileReader(configFile.toFile())) {
            ModConfig loadedConfig = GSON.fromJson(reader, ModConfig.class);
            if (loadedConfig != null) {
                this.config = loadedConfig;
            }
        } catch (IOException e) {
            System.err.println("[WIMF] Не удалось прочитать файл конфигурации!");
            e.printStackTrace();
        }
    }
}