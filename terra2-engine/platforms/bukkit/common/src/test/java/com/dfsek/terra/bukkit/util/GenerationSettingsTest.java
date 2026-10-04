package com.dfsek.terra.bukkit.util;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class GenerationSettingsTest {
    @TempDir Path folder;
    @Test void supportsSemicolonAndListSelectionsAndRefusesAmbiguity() throws Exception {
        Path file = folder.resolve("composition.yml");
        String prefix = "generation:\n  enabled: true\n  protected-worlds: [world]\nworlds:\n  test:\n";
        for(String entry : new String[]{"    pack: OVERWORLD;Dungeons-and-Taverns;Yggdrasil\n",
            "    packs: [OVERWORLD, Dungeons-and-Taverns, Yggdrasil]\n"}) {
            Files.writeString(file, prefix + entry);
            var config = GenerationSettings.load(file.toFile());
            assertEquals(java.util.List.of("OVERWORLD", "Dungeons-and-Taverns", "Yggdrasil"), GenerationSettings.packIds(config, "test"));
            assertEquals("OVERWORLD;Dungeons-and-Taverns;Yggdrasil", GenerationSettings.packSelection(config, "test"));
        }
        for(String entry : new String[]{"    pack: OVERWORLD;\n", "    pack: OVERWORLD;OVERWORLD\n", "    packs: [OVERWORLD, 42]\n",
            "    pack: OVERWORLD\n    packs: [Other]\n", "    packs: []\n"}) {
            Files.writeString(file, prefix + entry);
            assertThrows(InvalidConfigurationException.class, () -> GenerationSettings.load(file.toFile()));
        }
    }
    @Test void acceptsExplicitDatapackAndRejectsAmbiguousSources() throws Exception {
        Path file = folder.resolve("datapack-settings.yml");
        String valid = "generation:\n  enabled: true\n  protected-worlds: [world]\nworlds:\n  dp_test:\n    datapack: example.zip\n    dimension: test:flat\n";
        Files.writeString(file, valid);
        assertEquals("example.zip", GenerationSettings.load(file.toFile()).getString("worlds.dp_test.datapack"));
        for(String invalid : new String[]{valid.replace("example.zip", "../outside.zip"), valid + "    pack: OVERWORLD\n",
            valid.replace("test:flat", "bad key"), valid.replace("test:flat", "123"), valid.replace("dp_test", "world.name")}) {
            Files.writeString(file, invalid);
            assertThrows(InvalidConfigurationException.class, () -> GenerationSettings.load(file.toFile()));
        }
    }
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
