package org.terra2.core;
import java.util.Objects;
/** A world name identifies an authorized slot; dimension is bound once Paper creates it. */
public record WorldTarget(String worldName, String dimensionKey) {
    public WorldTarget {
        Objects.requireNonNull(worldName);
        if(worldName.isBlank()) throw new IllegalArgumentException("World name is empty");
        if(dimensionKey != null && !dimensionKey.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Invalid dimension key: " + dimensionKey);
    }
}
