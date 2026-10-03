package org.terra2.adapter.terra;
import org.terra2.core.*;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.api.world.chunk.generation.ProtoChunk;
import com.dfsek.terra.api.world.chunk.generation.util.Palette;
import com.dfsek.terra.api.world.info.WorldProperties;

/** Compatibility facade for old API callers; terrain and block samples are dispatched by the core. */
public final class CoreTerraGenerator implements ChunkGenerator {
    private final GenerationManager<BlockState, Biome> manager;
    private final String worldName;
    private final ChunkGenerator paletteDelegate;
    public CoreTerraGenerator(GenerationManager<BlockState, Biome> manager, String worldName, ChunkGenerator paletteDelegate) {
        this.manager = manager; this.worldName = worldName; this.paletteDelegate = paletteDelegate;
    }
    private GenerationContext context(WorldProperties world) {
        return new GenerationContext(manager.binding(worldName).target(), world.getSeed(), world.getMinHeight(),
            world.getMaxHeight(), world.getHandle());
    }
    public void generateChunkData(ProtoChunk chunk, WorldProperties world, BiomeProvider provider, int x, int z) {
        manager.generate(context(world), x, z, new BlockVolume<>() {
            public int minY() { return world.getMinHeight(); }
            public int maxY() { return chunk.getMaxHeight(); }
            public BlockState get(int x, int y, int z) { return chunk.getBlock(x, y, z); }
            public void set(int x, int y, int z, BlockState block) { chunk.setBlock(x, y, z, block); }
        });
    }
    public BlockState getBlock(WorldProperties world, int x, int y, int z, BiomeProvider provider) {
        return manager.blockAt(context(world), x, y, z);
    }
    public Biome getBiome(WorldProperties world, int x, int y, int z) {
        return manager.biomeAt(context(world), x, y, z);
    }
    public Palette getPalette(int x, int y, int z, WorldProperties world, BiomeProvider provider) {
        return paletteDelegate.getPalette(x, y, z, world, provider);
    }
}
