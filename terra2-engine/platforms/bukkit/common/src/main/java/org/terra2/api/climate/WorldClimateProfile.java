package org.terra2.api.climate;

import java.util.Locale;
import java.util.Set;

/** Read-only climate metadata. Never a request to replace a world's biome. */
public record WorldClimateProfile(String season, String referenceBiome) {
    public WorldClimateProfile {
        season = season == null ? "" : season.trim().toUpperCase(Locale.ROOT);
        if(!Set.of("SPRING", "SUMMER", "AUTUMN", "WINTER").contains(season))
            throw new IllegalArgumentException("Invalid climate season: " + season);
        referenceBiome = referenceBiome == null ? "" : referenceBiome.trim().toLowerCase(Locale.ROOT);
        if(!referenceBiome.contains(":")) referenceBiome = "minecraft:" + referenceBiome;
        if(!referenceBiome.matches("minecraft:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Climate reference must be a vanilla biome: " + referenceBiome);
    }
}
