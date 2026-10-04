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
    private GenerationPlan<BlockData, Biome> compile(org.terra2.adapter.vanilla.FlatDefinition definition) {
        if(definition.layers().stream().mapToInt(FlatDefinition.Layer::height).sum() > 382)
            throw new IllegalArgumentException("Flat world needs at least two air blocks above terrain for spawn");
        return definition.compile(block -> {
            BlockData data = Bukkit.createBlockData(block);
            if(!data.getMaterial().isBlock()) throw new IllegalArgumentException("Not a block: " + block);
            return data;
        }, biome -> {
            Biome value = Registry.BIOME.get(Objects.requireNonNull(NamespacedKey.fromString(biome)));
            if(value == null) throw new IllegalArgumentException("Unknown biome: " + biome);
            return value;
        });
    }
    public synchronized FlatChunkGenerator preparePacks(String world, String selection, List<org.terra2.pack.TerraPack> packs) {
        org.terra2.pack.TerraPackStore.validateComposition(packs, false);
        String fingerprint = String.join("+", packs.stream().map(org.terra2.pack.TerraPack::fingerprint).toList());
        FlatChunkGenerator previous = generators.get(world);
        if(previous != null) {
            if(!previous.selection().equals(selection) || !previous.fingerprint().equals(fingerprint))
                throw new IllegalArgumentException("Cannot replace active TerraPack composition: " + world);
            return previous;
        }
        var original = packs.stream().filter(pack -> pack.terrain() != null).findFirst().orElseThrow().terrain();
        var definition = new FlatDefinition(original.dimension(), original.biome(), original.layers(), fingerprint);
        var plan = org.terra2.pack.PlanComposition.compose(selection, fingerprint, compile(definition),
            org.terra2.pack.TerraPackStore.features(packs), block -> {
                var data = Bukkit.createBlockData(block);
                if(!data.getMaterial().isSolid()) throw new IllegalArgumentException("Simple-block feature requires a solid block: " + block);
                return data;
            }, data -> data.getMaterial().isAir());
        core.authorize(new WorldTarget(world, null), plan);
        var generator = new FlatChunkGenerator(core, world, selection, definition);
        if(!org.terra2.pack.TerraPackStore.features(packs).isEmpty()) generator.setExtraPopulators(List.of(
            new CoreDecorationPopulator<>(core, world, data -> data, data -> data)));
        generators.put(world, generator); return generator;
    }
    public boolean hasWorld(String world) { return generators.containsKey(world); }
    public synchronized void validatePackReload(String world, String selection, List<org.terra2.pack.TerraPack> packs) {
        var existing = generators.get(world);
        if(existing != null && (!existing.selection().equals(selection) ||
            !existing.fingerprint().equals(String.join("+", packs.stream().map(org.terra2.pack.TerraPack::fingerprint).toList()))))
            throw new IllegalArgumentException("Cannot replace active TerraPack composition: " + world);
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
        GenerationPlan<BlockData, Biome> plan = compile(definition);
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
        // Source-exclusive transitions are checked by the plugin's full composition validator.
    }
}
