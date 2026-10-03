package org.terra2.core;
import java.util.Objects;
/** maxY is exclusive. The opaque platform handle belongs to adapters, never the core. */
public record GenerationContext(WorldTarget target, long seed, int minY, int maxY, Object platformHandle) {
    public GenerationContext {
        Objects.requireNonNull(target);
        if(minY >= maxY) throw new IllegalArgumentException("Invalid vertical bounds");
    }
}
