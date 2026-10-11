package com.dfsek.terra.bukkit.compatibility;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.bukkit.configuration.InvalidConfigurationException;
import com.dfsek.terra.bukkit.util.GenerationSettings;
import static org.junit.jupiter.api.Assertions.*;
class BiomeCompatibilitySettingsTest {
    @TempDir Path folder;
    @Test void acceptsExplicitModeAndRejectsUnsafeReferenceTypes() throws Exception {
        Path file=folder.resolve("settings.yml");
        String prefix="generation:\n  enabled: false\n  protected-worlds: [world]\nworlds:\n  test:\n    pack: HYDRAXIA\n";
        Files.writeString(file,prefix+"    biomes:\n      mode: VANILLA_COMPATIBLE\n      references:\n        SNOW: minecraft:snowy_plains\n");
        assertEquals("VANILLA_COMPATIBLE",GenerationSettings.load(file.toFile()).getString("worlds.test.biomes.mode"));
        for(String value:new String[]{"    biomes: true\n","    biomes:\n      mode: 42\n","    biomes:\n      mode: vanilla\n",
            "    biomes:\n      references: [snow]\n","    biomes:\n      references:\n        SNOW: terra:fake\n","    biomes:\n      references:\n        SNOW: 12\n"}) {
            Files.writeString(file,prefix+value);
            assertThrows(InvalidConfigurationException.class,()->GenerationSettings.load(file.toFile()));
        }
    }
}
