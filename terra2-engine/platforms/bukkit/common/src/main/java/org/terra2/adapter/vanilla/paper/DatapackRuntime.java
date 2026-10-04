package org.terra2.adapter.vanilla.paper;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.terra2.adapter.vanilla.*;
import org.terra2.core.*;

/** Paper bridge; imported datapacks remain private to explicitly authorized world plans. */
public final class DatapackRuntime {
    private final Path directory;
    private final GenerationManager<BlockData, Biome> core;
    private final Map<String, FlatChunkGenerator> generators = new HashMap<>();
    public DatapackRuntime(Path directory, Set<String> protectedWorlds) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        Files.createDirectories(this.directory);
        core = new GenerationManager<>(protectedWorlds);
    }
    public List<String> list() throws IOException {
        try(var files = Files.list(directory)) {
            return files.filter(file -> !Files.isSymbolicLink(file) &&
                (Files.isDirectory(file) || file.getFileName().toString().endsWith(".zip")))
                .map(file -> file.getFileName().toString()).sorted().toList();
        }
    }
    public DatapackReader read(String file) throws IOException {
        if(!file.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]*") || file.equals(".."))
            throw new IllegalArgumentException("Use only a datapack folder name or ZIP filename, without a path");
        Path target = directory.resolve(file).normalize();
        if(!target.getParent().equals(directory)) throw new IllegalArgumentException("Invalid datapack path");
        return DatapackReader.open(target);
    }
    public static String selection(YamlConfiguration settings, String world) {
        return settings.getString("worlds." + world + ".datapack") + "#" + settings.getString("worlds." + world + ".dimension");
    }
    public synchronized FlatChunkGenerator prepare(String world, YamlConfiguration settings) throws IOException {
        String selected = selection(settings, world);
        FlatChunkGenerator previous = generators.get(world);
        if(previous != null) {
            if(!previous.selection().equals(selected)) throw new IllegalArgumentException("Cannot change the datapack of an active world: " + world);
            return previous;
        }
        FlatDefinition definition = read(settings.getString("worlds." + world + ".datapack"))
            .flat(settings.getString("worlds." + world + ".dimension"));
        GenerationPlan<BlockData, Biome> plan = definition.compile(block -> {
            BlockData data = Bukkit.createBlockData(block);
            if(!data.getMaterial().isBlock()) throw new IllegalArgumentException("Not a block: " + block);
            return data;
        }, biome -> {
            Biome value = Registry.BIOME.get(Objects.requireNonNull(NamespacedKey.fromString(biome)));
            if(value == null) throw new IllegalArgumentException("Unknown biome: " + biome);
            return value;
        });
        core.authorize(new WorldTarget(world, null), plan);
        FlatChunkGenerator generator = new FlatChunkGenerator(core, world, selected, definition);
        generators.put(world, generator);
        return generator;
    }
    public synchronized void validateReload(YamlConfiguration next) throws IOException {
        var worlds = next.getConfigurationSection("worlds");
        for(String world : worlds.getKeys(false)) if(next.isString("worlds." + world + ".datapack")) {
            FlatChunkGenerator existing = generators.get(world);
            if(existing != null && !existing.selection().equals(selection(next, world)))
                throw new IllegalArgumentException("Cannot change the datapack of an active world: " + world);
            var definition = read(next.getString("worlds." + world + ".datapack")).flat(next.getString("worlds." + world + ".dimension"));
            if(existing != null && !existing.fingerprint().equals(definition.fingerprint()))
                throw new IllegalArgumentException("Datapack content changed for active world: " + world);
        }
        for(String world : generators.keySet()) if(next.isString("worlds." + world + ".pack"))
            throw new IllegalArgumentException("Cannot replace an active datapack generator with a Terra pack: " + world);
    }
}
