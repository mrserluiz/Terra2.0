package com.dfsek.terra.bukkit.util;

import java.util.*;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.terra2.api.climate.WorldClimateProfile;
import org.terra2.api.climate.WorldClimateService;

/** Immutable snapshots are replaced only after the complete settings reload is valid. */
public final class TerraWorldClimateService implements WorldClimateService {
    public record Snapshot(boolean enabled, Map<String, WorldClimateProfile> worlds, Set<String> protectedWorlds) {}
    private volatile Snapshot snapshot = new Snapshot(false, Map.of(), Set.of());

    public static Snapshot prepare(YamlConfiguration settings, String primaryWorld) {
        Set<String> protectedWorlds = new HashSet<>();
        for(String name : settings.getStringList("generation.protected-worlds")) protectedWorlds.add(name.toLowerCase(Locale.ROOT));
        for(String name : List.of(primaryWorld, primaryWorld + "_nether", primaryWorld + "_the_end", "world", "world_nether", "world_the_end"))
            protectedWorlds.add(name.toLowerCase(Locale.ROOT));
        Map<String, WorldClimateProfile> profiles = new HashMap<>();
        var worlds = settings.getConfigurationSection("worlds");
        if(worlds != null) for(String name : worlds.getKeys(false)) {
            var climate = worlds.getConfigurationSection(name + ".climate");
            if(climate == null || !climate.getBoolean("enabled", true)) continue;
            if(protectedWorlds.contains(name.toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Climate profile cannot target a protected world: " + name);
            var profile = new WorldClimateProfile(climate.getString("season"), climate.getString("reference-biome"));
            if(Registry.BIOME.get(NamespacedKey.fromString(profile.referenceBiome())) == null)
                throw new IllegalArgumentException("Unknown vanilla climate reference: " + profile.referenceBiome());
            profiles.put(name, profile);
        }
        return new Snapshot(settings.getBoolean("climate.enabled", false) && settings.getBoolean("generation.enabled"),
            Map.copyOf(profiles), Set.copyOf(protectedWorlds));
    }

    public void apply(Snapshot next) { snapshot = Objects.requireNonNull(next); }
    public int apiVersion() { return 1; }
    public Optional<WorldClimateProfile> profile(World world) {
        Snapshot current = snapshot;
        if(world == null || !current.enabled() || current.protectedWorlds().contains(world.getName().toLowerCase(Locale.ROOT)))
            return Optional.empty();
        if(!(world.getGenerator() instanceof com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper)
            && !(world.getGenerator() instanceof org.terra2.adapter.vanilla.paper.FlatChunkGenerator)) return Optional.empty();
        return Optional.ofNullable(current.worlds().get(world.getName()));
    }
    public Map<String, String> describe(World world) {
        return profile(world).map(p -> Map.of("schema", "1", "source", "Terra2", "world", world.getName(),
            "world-uuid", world.getUID().toString(), "dimension", world.getKey().toString(),
            "season", p.season(), "reference-biome", p.referenceBiome())).orElseGet(Map::of);
    }
}
