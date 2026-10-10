package com.dfsek.terra.bukkit.util;

import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class WorldSettingsEditorTest {
    @TempDir Path folder;
    private YamlConfiguration settings() throws Exception {
        var config = new YamlConfiguration();
        config.loadFromString("language: pt_BR\ngeneration:\n  enabled: false\n  protected-worlds: [reserved]\nworlds:\n  old:\n    pack: OVERWORLD\n    loot:\n      enabled: true\n");
        return config;
    }
    @Test void allBaseNamesCustomPrimaryAndVanillaKeysAreBlockedEvenWithoutYamlProtection() throws Exception {
        var config = settings();
        for(String world : new String[]{"world", "WORLD", "nether", "end", "world_nether", "world_the_end", "the_nether", "the_end",
            "EtherCraft", "ethercraft_nether", "ETHERCRAFT_the_end", "reserved"}) {
            assertTrue(WorldSettingsEditor.blocked(config, "EtherCraft", world, null), world);
            assertThrows(IllegalArgumentException.class, () -> WorldSettingsEditor.select(config, "EtherCraft", world, null, false, null, "OVERWORLD"));
        }
        for(String key : new String[]{"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"})
            assertTrue(WorldSettingsEditor.blocked(config, "EtherCraft", "apparently_custom", key));
        assertTrue(WorldSettingsEditor.blocked(config, null, "new", null));
        assertFalse(WorldSettingsEditor.blocked(config, "EtherCraft", "new", "terra2:new"));
        assertFalse(config.getBoolean("generation.enabled"));
    }
    @Test void authorizationPreservesOtherWorldsLootAndProtectionAndSurvivesReload() throws Exception {
        var current = settings();
        var next = WorldSettingsEditor.select(current, "world", "old", null, false, null, "OVERWORLD;Debris");
        assertFalse(current.getBoolean("generation.enabled"));
        assertEquals("OVERWORLD", current.getString("worlds.old.pack"));
        assertTrue(next.getBoolean("worlds.old.loot.enabled"));
        assertEquals(current.getStringList("generation.protected-worlds"), next.getStringList("generation.protected-worlds"));
        var file = folder.resolve("settings.yml");
        current.save(file.toFile());
        WorldSettingsEditor.persist(file, next);
        var restored = GenerationSettings.load(file.toFile());
        assertEquals(java.util.List.of("OVERWORLD", "Debris"), GenerationSettings.packIds(restored, "old"));
        assertTrue(restored.getBoolean("generation.enabled"));
        assertTrue(restored.getBoolean("worlds.old.loot.enabled"));
        assertEquals(1, restored.getConfigurationSection("worlds").getKeys(false).size());
    }
    @Test void loadedGeneratorReplacementInvalidNamesAndCaseCollisionsAreRefused() throws Exception {
        var current = settings();
        assertThrows(IllegalArgumentException.class, () -> WorldSettingsEditor.select(current, "world", "new", null, true, null, "OVERWORLD"));
        assertThrows(IllegalArgumentException.class, () -> WorldSettingsEditor.select(current, "world", "old", null, true, "OVERWORLD", "TARTARUS"));
        assertDoesNotThrow(() -> WorldSettingsEditor.select(current, "world", "old", null, true, "OVERWORLD", "OVERWORLD"));
        for(String name : new String[]{"../world", "world.name", "bad:name", "OLD", ""})
            assertThrows(IllegalArgumentException.class, () -> WorldSettingsEditor.select(current, "world", name, null, false, null, "OVERWORLD"));
        assertThrows(IllegalArgumentException.class, () -> WorldSettingsEditor.select(current, "world", "new", null, false, null, "OVERWORLD;"));
    }
    @Test void failedPersistenceNeverReplacesExistingSettings() throws Exception {
        var file = folder.resolve("settings.yml");
        var current = settings(); current.save(file.toFile());
        String before = Files.readString(file);
        current.set("generation.enabled", "bad");
        assertThrows(Exception.class, () -> WorldSettingsEditor.persist(file, current));
        assertEquals(before, Files.readString(file));
        try(var files = Files.list(folder)) { assertEquals(1, files.count()); }
    }
}
