package org.terra2.adapter.vanilla;

import java.util.List;
import java.util.function.Function;
import org.terra2.core.*;

/** Immutable neutral representation of the supported vanilla flat generator. */
public record FlatDefinition(String dimension, String biome, List<Layer> layers, String fingerprint) {
    public record Layer(int height, String block) {}
    public FlatDefinition { layers = List.copyOf(layers); }
    public <B, M> GenerationPlan<B, M> compile(Function<String, B> blocks, Function<String, M> biomes) {
        List<B> palette = layers.stream().map(layer -> blocks.apply(layer.block())).toList();
        B air = blocks.apply("minecraft:air");
        M biomeState = biomes.apply(biome);
        int total = layers.stream().mapToInt(Layer::height).sum();
        return new GenerationPlan<>(dimension, "vanilla-datapack", fingerprint, new GenerationProgram<>() {
            private void check(GenerationContext context) {
                if(total > context.maxY() - context.minY())
                    throw new IllegalArgumentException("Flat layers exceed the world's height: " + dimension);
            }
            public B blockAt(GenerationContext context, int x, int y, int z) {
                check(context);
                if(y < context.minY() || y >= context.maxY()) return air;
                int offset = y - context.minY();
                for(int index = 0; index < layers.size(); index++) {
                    if(offset < layers.get(index).height()) return palette.get(index);
                    offset -= layers.get(index).height();
                }
                return air;
            }
            public M biomeAt(GenerationContext context, int x, int y, int z) { check(context); return biomeState; }
            public void generate(GenerationContext context, int chunkX, int chunkZ, BlockVolume<B> output) {
                check(context);
                int y = context.minY();
                for(int index = 0; index < layers.size(); index++) {
                    int end = y + layers.get(index).height();
                    for(; y < end; y++) for(int x = 0; x < 16; x++) for(int z = 0; z < 16; z++)
                        output.set(x, y, z, palette.get(index));
                }
            }
        });
    }
}
