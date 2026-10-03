package com.dfsek.terra.bukkit.util;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class GenerationSettingsTest {
    @TempDir Path folder;
    @Test void loadsNewWorldAndRejectsMalformedOrMistypedSettings() throws Exception {
        Path file = folder.resolve("settings.yml");
        String valid = "generation:\n  enabled: true\n  protected-worlds: [world]\nworlds:\n  test:\n    pack: OVERWORLD\n";
        Files.writeString(file, valid);
        var previous = GenerationSettings.load(file.toFile());
        assertEquals("OVERWORLD", previous.getString("worlds.test.pack"));
        for(String invalid : new String[] { valid.replace("true", "'true'"), valid.replace("OVERWORLD", "42"), "worlds: [" }) {
            Files.writeString(file, invalid);
            assertThrows(InvalidConfigurationException.class, () -> GenerationSettings.load(file.toFile()));
            assertEquals("OVERWORLD", previous.getString("worlds.test.pack"));
        }
        Files.writeString(file, "generation:\n  enabled: false\n  protected-worlds: []\nworlds: {}\n");
        assertFalse(GenerationSettings.load(file.toFile()).getBoolean("generation.enabled"));
    }
}
