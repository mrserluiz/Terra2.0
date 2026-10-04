package com.dfsek.terra.bukkit.util;
import org.junit.jupiter.api.Test;
import org.terra2.adapter.terra.CoreTerraGenerator;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.api.world.chunk.generation.ProtoChunk;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.info.WorldProperties;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.chunk.generation.util.Palette;
import static org.junit.jupiter.api.Assertions.*;
class GeneratorCapabilityTest {
    private static final class Backend implements ChunkGenerator {
        public void generateChunkData(ProtoChunk chunk, WorldProperties world, BiomeProvider biomes, int x, int z) {}
        public BlockState getBlock(WorldProperties world, int x, int y, int z, BiomeProvider biomes) { return null; }
        public Palette getPalette(int x, int y, int z, WorldProperties world, BiomeProvider biomes) { return null; }
    }
    @Test void oldAddonsCanResolveBackendWithoutCastingTheCoreFacade() {
        var backend = new Backend();
        var facade = new CoreTerraGenerator(null, "test", backend);
        assertSame(backend, facade.requireCapability(Backend.class));
        assertSame(facade, facade.requireCapability(ChunkGenerator.class));
        assertThrows(IllegalStateException.class, () -> facade.requireCapability(String.class));
    }
}
