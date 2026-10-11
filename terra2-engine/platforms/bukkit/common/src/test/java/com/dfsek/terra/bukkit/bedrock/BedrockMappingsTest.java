package com.dfsek.terra.bukkit.bedrock;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class BedrockMappingsTest {
    @TempDir Path folder;
    @Test void writesCanonicalCustomIdsWithoutChangingVanillaOrOtherMappings() throws Exception {
        var colors = new JsonObject(); var effects = new JsonObject();
        effects.addProperty("grass_color", "#3366FF"); colors.add("effects", effects);
        var doc = BedrockMappings.document(Map.of("terra:hydraxia/hydraxia/sakura_woodlands", colors));
        Path target = folder.resolve("terra2-biomes.json"), other = folder.resolve("items.json");
        Files.writeString(other, "operator mapping");
        BedrockMappings.write(target, doc); String first = Files.readString(target);
        BedrockMappings.write(target, doc);
        assertEquals(first, Files.readString(target)); assertEquals("operator mapping", Files.readString(other));
        assertTrue(first.contains("terra:hydraxia/hydraxia/sakura_woodlands"));
        assertThrows(IllegalArgumentException.class, () -> BedrockMappings.document(Map.of("minecraft:plains", colors)));
    }
    @Test void refusesExistingOperatorFileAndMalformedFile() throws Exception {
        var colors = new JsonObject(); colors.add("effects", new JsonObject());
        var doc = BedrockMappings.document(Map.of("terra:test/test/biome", colors));
        Path target = folder.resolve("terra2-biomes.json");
        Files.writeString(target, "{\"biomes\":{}}");
        assertThrows(java.io.IOException.class, () -> BedrockMappings.write(target, doc));
        assertEquals("{\"biomes\":{}}", Files.readString(target));
        Files.writeString(target, "broken");
        assertThrows(java.io.IOException.class, () -> BedrockMappings.write(target, doc));
        assertEquals("broken", Files.readString(target));
    }
}
