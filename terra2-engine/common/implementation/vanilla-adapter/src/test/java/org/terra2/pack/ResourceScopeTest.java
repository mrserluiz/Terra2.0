package org.terra2.pack;

import com.google.gson.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ResourceScopeTest {
    @TempDir Path directory;
    private ResourceScope scope() throws Exception {
        for(String path : new String[]{"data/test/worldgen/template_pool/rooms.json", "data/test/loot_table/chest.json",
            "data/test/tags/worldgen/biome/forest.json", "data/test/function/game.mcfunction"}) {
            var file = directory.resolve(path); Files.createDirectories(file.getParent()); Files.writeString(file, "{}");
        }
        return new ResourceScope(ResourceBundle.read(directory));
    }
    @Test void isolatesResourcesAndNestedBiomeTagsWithoutChangingVanillaCodecIdsOrConnectorLabels() throws Exception {
        var scope = scope();
        assertEquals(scope.privateId("test:forest"), scope.tag("test:forest"));
        assertEquals("#" + scope.privateId("test:forest"), scope.tag("#test:forest"));
        var json = JsonParser.parseString("""
            {"type":"minecraft:jigsaw","start_pool":"test:rooms","start_jigsaw_name":"test:rooms",
             "biomes":"#test:forest","loot":"test:chest","function":"test:chest","other":"minecraft:empty",
             "components":{"minecraft:item_model":"test:chest"},"destination":"test:forest"}
            """);
        var result = scope.rewrite(json).getAsJsonObject();
        assertEquals("minecraft:jigsaw", result.get("type").getAsString());
        assertEquals(scope.privateId("test:rooms"), result.get("start_pool").getAsString());
        assertEquals("test:rooms", result.get("start_jigsaw_name").getAsString());
        assertEquals("#" + scope.privateId("test:forest"), result.get("biomes").getAsString());
        assertEquals(scope.privateId("test:chest"), result.get("loot").getAsString());
        assertEquals("test:chest", result.get("function").getAsString());
        assertEquals("minecraft:empty", result.get("other").getAsString());
        assertEquals("test:chest", result.getAsJsonObject("components").get("minecraft:item_model").getAsString());
        assertEquals(scope.privateId("test:forest"), result.get("destination").getAsString());
        assertEquals("test:game", scope.resource("test:game"));
        assertEquals("test:rooms", json.getAsJsonObject().get("start_pool").getAsString());
    }
}
