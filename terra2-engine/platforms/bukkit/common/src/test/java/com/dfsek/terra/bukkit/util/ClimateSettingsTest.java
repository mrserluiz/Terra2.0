package com.dfsek.terra.bukkit.util;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.bukkit.configuration.InvalidConfigurationException;
import org.terra2.api.climate.WorldClimateProfile;
import static org.junit.jupiter.api.Assertions.*;

class ClimateSettingsTest {
    @TempDir Path folder;
    @Test void acceptsOptInAndRefusesMistypedOrCustomReference() throws Exception {
        Path file = folder.resolve("settings.yml");
        String prefix = "generation:\n  enabled: true\n  protected-worlds: [world]\nclimate:\n  enabled: true\nworlds:\n  snow_test:\n    pack: HYDRAXIA\n";
        String profile = "    climate:\n      season: WINTER\n      reference-biome: minecraft:snowy_plains\n";
        Files.writeString(file, prefix + profile);
        assertTrue(GenerationSettings.load(file.toFile()).getBoolean("climate.enabled"));
        for(String invalid : new String[]{prefix + profile.replace("WINTER", "storm"), prefix + profile.replace("minecraft:snowy_plains", "terra:custom"),
                prefix + profile.replace("season: WINTER", "season: 42"), prefix.replace("climate:\n  enabled: true", "climate:\n  enabled: 'true'") + profile,
                prefix + "    climate: winter\n"}) {
            Files.writeString(file, invalid);
            assertThrows(InvalidConfigurationException.class, () -> GenerationSettings.load(file.toFile()));
        }
    }
    @Test void normalizesReferencesAndRequiresExplicitSeason() {
        var profile = new WorldClimateProfile(" winter ", "SNOWY_PLAINS");
        assertEquals("WINTER", profile.season()); assertEquals("minecraft:snowy_plains", profile.referenceBiome());
        assertThrows(IllegalArgumentException.class, () -> new WorldClimateProfile(null, "snowy_plains"));
        assertThrows(IllegalArgumentException.class, () -> new WorldClimateProfile("WINTER", null));
    }
}
