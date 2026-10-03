package com.dfsek.terra.bukkit.util;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class GenerationSettings {
    private GenerationSettings() {}
    public static YamlConfiguration load(File file) throws IOException, InvalidConfigurationException {
        var settings = new YamlConfiguration();
        settings.load(file);
        if(!settings.isBoolean("generation.enabled"))
            throw new InvalidConfigurationException("generation.enabled deve ser true ou false");
        if(!settings.isList("generation.protected-worlds") || settings.getList("generation.protected-worlds").stream()
                .anyMatch(value -> !(value instanceof String)))
            throw new InvalidConfigurationException("generation.protected-worlds deve ser uma lista de nomes");
        var worlds = settings.getConfigurationSection("worlds");
        if(worlds == null) throw new InvalidConfigurationException("worlds deve ser uma seção YAML");
        for(String world : worlds.getKeys(false)) {
            Object pack = worlds.get(world + ".pack");
            if(!(pack instanceof String id) || id.isBlank())
                throw new InvalidConfigurationException("Pack inválido para " + world);
        }
        return settings;
    }
}
