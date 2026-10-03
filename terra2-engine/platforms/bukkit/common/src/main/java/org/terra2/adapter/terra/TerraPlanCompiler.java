package org.terra2.adapter.terra;
import org.terra2.core.*;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.api.world.info.WorldProperties;
import com.dfsek.terra.api.world.chunk.generation.ProtoChunk;

/** First migration backend: executes existing Terra behavior under neutral core contracts. */
public final class TerraPlanCompiler {
    private TerraPlanCompiler() {}
    public static GenerationPlan<BlockState, Biome> compile(ConfigPack pack) {
        return compile(pack, pack.getGeneratorProvider().newInstance(pack));
    }
    public static GenerationPlan<BlockState, Biome> compile(ConfigPack pack,
        com.dfsek.terra.api.world.chunk.generation.ChunkGenerator generator) {
        var biomes = pack.getBiomeProvider();
        return new GenerationPlan<>(pack.getID(), "terra", pack.getVersion().toString(), new GenerationProgram<>() {
            private WorldProperties world(GenerationContext context) {
                return new WorldProperties() {
                    public long getSeed() { return context.seed(); }
                    public int getMinHeight() { return context.minY(); }
                    public int getMaxHeight() { return context.maxY(); }
                    public Object getHandle() { return context.platformHandle(); }
                    public boolean equals(Object other) {
                        return other instanceof WorldProperties properties && java.util.Objects.equals(getHandle(), properties.getHandle());
                    }
                    public int hashCode() { return java.util.Objects.hashCode(getHandle()); }
                };
            }
            public void generate(GenerationContext context, int x, int z, BlockVolume<BlockState> output) {
                generator.generateChunkData(new ProtoChunk() {
                    public int getMaxHeight() { return output.maxY(); }
                    public Object getHandle() { return output; }
                    public BlockState getBlock(int x, int y, int z) { return output.get(x, y, z); }
                    public void setBlock(int x, int y, int z, BlockState block) { output.set(x, y, z, block); }
                }, world(context), biomes, x, z);
            }
            public BlockState blockAt(GenerationContext context, int x, int y, int z) {
                return generator.getBlock(world(context), x, y, z, biomes);
            }
            public Biome biomeAt(GenerationContext context, int x, int y, int z) {
                return biomes.getBiome(x, y, z, context.seed());
            }
        });
    }
}
