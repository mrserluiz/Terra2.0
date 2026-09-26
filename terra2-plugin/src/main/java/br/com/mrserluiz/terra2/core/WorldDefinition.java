package br.com.mrserluiz.terra2.core;

import java.util.Locale;
import java.util.Objects;

/** Explicit administrator authorization for one world and one Terra pack. */
public record WorldDefinition(
        String worldName,
        boolean enabled,
        boolean createIfMissing,
        String packId,
        String environment,
        Long seed
) {
    public WorldDefinition {
        worldName = requireText(worldName, "worldName");
        packId = requireText(packId, "packId");
        environment = requireText(environment, "environment").toUpperCase(Locale.ROOT);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return normalized;
    }
}
