package org.terra2.pack;

import java.util.SplittableRandom;
import java.util.function.Predicate;
import org.terra2.core.*;

/** Neutral compiled feature. Terra2's deterministic sampler is independent of vanilla PRNG internals. */
public record SimpleBlockFeature(String id, String block, int count, int rarity) {
    public <B> void decorate(GenerationContext context, int chunkX, int chunkZ, BlockVolume<B> volume, B state, Predicate<B> air) {
        long salt = context.seed() ^ (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L ^ id.hashCode();
        var random = new SplittableRandom(salt);
        for(int attempt = 0; attempt < count; attempt++) {
            if(random.nextInt(rarity) != 0) continue;
            int x = random.nextInt(16), z = random.nextInt(16);
            for(int y = context.maxY() - 2; y >= context.minY(); y--) {
                if(!air.test(volume.get(x, y, z))) {
                    if(air.test(volume.get(x, y + 1, z))) volume.set(x, y + 1, z, state);
                    break;
                }
            }
        }
    }
}
