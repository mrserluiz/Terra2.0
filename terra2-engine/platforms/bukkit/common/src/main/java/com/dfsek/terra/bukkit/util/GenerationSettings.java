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
            if(!world.matches("[A-Za-z0-9_-]+"))
                throw new InvalidConfigurationException("Nome de mundo inválido: " + world);
            var entry = worlds.getConfigurationSection(world);
            if(entry == null) throw new InvalidConfigurationException("Mundo deve ser uma seção YAML: " + world);
            boolean terra = entry.contains("pack"), vanilla = entry.contains("datapack");
            if(terra == vanilla) throw new InvalidConfigurationException("Defina pack OU datapack para " + world);
            if(vanilla) {
                Object sourceFile = entry.get("datapack"), dimension = entry.get("dimension");
                if(!(sourceFile instanceof String name) || !name.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]*")
                        || !(dimension instanceof String key) || !key.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                    throw new InvalidConfigurationException("Datapack/dimension inválidos para " + world);
                continue;
            }
            if(entry.contains("dimension")) throw new InvalidConfigurationException("dimension requer datapack: " + world);
            Object pack = worlds.get(world + ".pack");
            if(!(pack instanceof String id) || id.isBlank())
                throw new InvalidConfigurationException("Pack inválido para " + world);
        }
        return settings;
    }
}
