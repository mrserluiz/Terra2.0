package com.dfsek.terra.bukkit.util;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class GenerationSettings {
    public static java.util.List<String> packIds(YamlConfiguration settings, String world) {
        var entry = settings.getConfigurationSection("worlds." + world);
        if(entry == null) return java.util.List.of();
        Object raw = entry.contains("packs") ? entry.get("packs") : entry.get("pack");
        if(raw == null) return java.util.List.of();
        java.util.List<String> ids;
        if(raw instanceof String text) ids = java.util.Arrays.stream(text.split(";", -1)).map(String::trim).toList();
        else if(raw instanceof java.util.List<?> list && list.stream().allMatch(String.class::isInstance))
            ids = list.stream().map(value -> ((String) value).trim()).toList();
        else throw new IllegalArgumentException("pack/packs deve ser uma string ou lista de IDs");
        if(ids.isEmpty() || ids.size() > 8 || ids.stream().anyMatch(id -> !id.matches("[A-Za-z0-9_][A-Za-z0-9_.:/-]{0,127}"))
                || new java.util.HashSet<>(ids).size() != ids.size())
            throw new IllegalArgumentException("Defina 1..8 IDs distintos, sem entradas vazias");
        return ids;
    }
    public static String packSelection(YamlConfiguration settings, String world) { return String.join(";", packIds(settings, world)); }

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
            if(entry.contains("pack") && entry.contains("packs")) throw new InvalidConfigurationException("Use pack OU packs: " + world);
            boolean terra = entry.contains("pack") || entry.contains("packs"), vanilla = entry.contains("datapack");
            if(terra == vanilla) throw new InvalidConfigurationException("Defina pack OU datapack para " + world);
            if(vanilla) {
                Object sourceFile = entry.get("datapack"), dimension = entry.get("dimension");
                if(!(sourceFile instanceof String name) || !name.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]*")
                        || !(dimension instanceof String key) || !key.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                    throw new InvalidConfigurationException("Datapack/dimension inválidos para " + world);
                continue;
            }
            if(entry.contains("dimension")) throw new InvalidConfigurationException("dimension requer datapack: " + world);
            try { packIds(settings, world); }
            catch(IllegalArgumentException error) { throw new InvalidConfigurationException("Pack inválido para " + world + ": " + error.getMessage()); }
        }
        return settings;
    }
}
