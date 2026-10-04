package org.terra2.adapter.vanilla.paper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.bukkit.World;
import org.bukkit.HeightMap;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.*;
import org.terra2.adapter.vanilla.FlatDefinition;
import org.terra2.core.*;

/** Flat terrain runs through the same neutral core contracts, with no Terra noise/addon dependency. */
public final class FlatChunkGenerator extends ChunkGenerator {
    private final GenerationManager<BlockData, Biome> core;
    private final String world, selection, fingerprint;
    private final FlatDefinition definition;
    private volatile String dimensionKey;
    FlatChunkGenerator(GenerationManager<BlockData, Biome> core, String world, String selection, FlatDefinition definition) {
        this.core = core; this.world = world; this.selection = selection;
        this.definition = definition; this.fingerprint = definition.fingerprint();
    }
    public String selection() { return selection; }
    public String fingerprint() { return fingerprint; }
    public synchronized void bind(World loaded) throws IOException {
        if(!loaded.getName().equals(world) || loaded.getEnvironment() != World.Environment.NORMAL
                || loaded.getMinHeight() != -64 || loaded.getMaxHeight() != 320)
            throw new IllegalArgumentException("Datapack flat requires an OVERWORLD world with height -64..320");
        core.bindDimension(world, loaded.getKey().toString());
        Path manifest = loaded.getWorldFolder().toPath().resolve("terra2-generation.json");
        String identity = core.binding(world).plan().identity();
        if(Files.exists(manifest)) {
            var json = JsonParser.parseString(Files.readString(manifest)).getAsJsonObject();
            if(!identity.equals(json.get("plan").getAsString()) || !selection.equals(json.get("selection").getAsString())
                    || !loaded.getKey().toString().equals(json.get("dimension").getAsString())
                    || loaded.getSeed() != json.get("seed").getAsLong())
                throw new IllegalArgumentException("Existing world manifest differs; refusing generator change: " + world);
        } else {
            var json = new JsonObject();
            json.addProperty("schema", 1); json.addProperty("world", world);
            json.addProperty("dimension", loaded.getKey().toString()); json.addProperty("seed", loaded.getSeed());
            json.addProperty("plan", identity); json.addProperty("selection", selection);
            json.addProperty("sourceDimension", definition.dimension());
            json.addProperty("minecraft", "26.2"); json.addProperty("minY", -64); json.addProperty("maxY", 320);
            Files.writeString(manifest, json.toString() + "\n", StandardOpenOption.CREATE_NEW);
        }
        // Publish only after manifest validation/write; a failed init cannot produce chunks.
        dimensionKey = loaded.getKey().toString();
    }
    private GenerationContext context(WorldInfo info) {
        if(dimensionKey == null) throw new IllegalStateException("Datapack world initialization/manifest did not complete: " + world);
        return new GenerationContext(new WorldTarget(world, dimensionKey), info.getSeed(), info.getMinHeight(), info.getMaxHeight(), null);
    }
    @Override public void generateNoise(WorldInfo info, Random random, int x, int z, ChunkData data) {
        try {
            core.generate(context(info), x, z, new BlockVolume<>() {
                public void set(int bx, int y, int bz, BlockData state) { data.setBlock(bx, y, bz, state); }
                public BlockData get(int bx, int y, int bz) { return data.getBlockData(bx, y, bz); }
                public int minY() { return data.getMinHeight(); }
                public int maxY() { return data.getMaxHeight(); }
            });
        } catch(RuntimeException | LinkageError error) {
            com.dfsek.terra.bukkit.util.GenerationReport.failure("datapack-noise " + x + "," + z, world, selection, error);
            throw error;
        }
    }
    @Override public BiomeProvider getDefaultBiomeProvider(WorldInfo info) {
        // Paper can query biome source while constructing the world before WorldInitEvent.
        Biome biome = org.bukkit.Registry.BIOME.get(org.bukkit.NamespacedKey.fromString(definition.biome()));
        return new BiomeProvider() {
            public Biome getBiome(WorldInfo target, int x, int y, int z) {
                return dimensionKey == null ? biome : core.biomeAt(context(target), x, y, z);
            }
            public List<Biome> getBiomes(WorldInfo target) { return List.of(biome); }
        };
    }
    @Override public int getBaseHeight(WorldInfo info, Random random, int x, int z, HeightMap heightMap) {
        var context = context(info);
        int end = info.getMinHeight() + definition.layers().stream().mapToInt(FlatDefinition.Layer::height).sum();
        for(int y = end - 1; y >= info.getMinHeight(); y--) {
            var material = core.blockAt(context, x, y, z).getMaterial();
            boolean occupied = switch(heightMap) {
                case OCEAN_FLOOR, OCEAN_FLOOR_WG -> material.isSolid();
                case MOTION_BLOCKING_NO_LEAVES -> !material.name().endsWith("_LEAVES") &&
                    (material.isSolid() || material == org.bukkit.Material.WATER || material == org.bukkit.Material.LAVA);
                case MOTION_BLOCKING -> material.isSolid() || material == org.bukkit.Material.WATER || material == org.bukkit.Material.LAVA;
                default -> !material.isAir();
            };
            if(occupied) return y + 1;
        }
        return info.getMinHeight();
    }
    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }
    @Override public boolean shouldGenerateBedrock() { return false; }
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }
    @Override public boolean shouldGenerateMobs() { return false; }
}
