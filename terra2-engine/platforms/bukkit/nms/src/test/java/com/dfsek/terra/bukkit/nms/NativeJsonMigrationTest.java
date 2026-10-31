package com.dfsek.terra.bukkit.nms;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NativeJsonMigrationTest {
    @Test void preservesModernLootPoolReferencesAndSequenceOnRepeatedMigration() {
        var source=JsonParser.parseString("""
            {"type":"minecraft:chest","pools":[{"rolls":1,"condition":"example:eligible",
              "entries":[{"type":"minecraft:item","name":"minecraft:stone",
                "modifier":{"type":"minecraft:sequence","functions":[
                  {"type":"minecraft:set_count","count":3}]}}]}]}
            """);
        assertEquals(source,NativeJsonMigration.migrate(source));
        var legacy=JsonParser.parseString("""
            {"function":"minecraft:sequence","functions":[
              {"function":"minecraft:set_count","count":3}]}
            """);
        var migrated=NativeJsonMigration.migrate(legacy);
        assertTrue(migrated.getAsJsonObject().has("functions"));
        assertEquals(migrated,NativeJsonMigration.migrate(migrated));
        assertTrue(legacy.getAsJsonObject().has("function"));
    }
    @Test void migratesNestedEntityChecksWithoutRenamingFeatureOrItemTypes() {
        var source = JsonParser.parseString("""
            {"condition":"entity_properties","entity":"this","predicate":{
              "type":"minecraft:player","vehicle":{"type":"minecraft:horse"},
              "type_specific":{"type":"minecraft:player","looking_at":{"type":"minecraft:pig"}}}}
            """);
        var result = NativeJsonMigration.migrate(source).getAsJsonObject().getAsJsonObject("predicate");
        assertEquals("minecraft:player", result.get("minecraft:entity_type").getAsString());
        assertEquals("minecraft:horse", result.getAsJsonObject("vehicle").get("minecraft:entity_type").getAsString());
        assertEquals("minecraft:pig", result.getAsJsonObject("minecraft:type_specific/player").getAsJsonObject("looking_at").get("minecraft:entity_type").getAsString());
        assertTrue(source.getAsJsonObject().getAsJsonObject("predicate").has("type_specific"));
        var item = JsonParser.parseString("{\"type\":\"minecraft:item\",\"name\":\"minecraft:stone\"}");
        assertEquals(item, NativeJsonMigration.migrate(item));
    }
    @Test void clockMigrationIsIdempotentAndPreservesExplicitClocks() {
        var old = JsonParser.parseString("{\"condition\":\"time_check\",\"value\":12000}");
        var next = NativeJsonMigration.migrate(old);
        assertEquals("minecraft:overworld", next.getAsJsonObject().get("clock").getAsString());
        assertEquals(next, NativeJsonMigration.migrate(next)); assertFalse(old.getAsJsonObject().has("clock"));
        var custom = JsonParser.parseString("{\"condition\":\"time_check\",\"clock\":\"example:custom\",\"value\":12000}");
        var migrated=NativeJsonMigration.migrate(custom);
        assertEquals("example:custom",migrated.getAsJsonObject().get("clock").getAsString());
        assertEquals("time_check",migrated.getAsJsonObject().get("type").getAsString());
        assertEquals(migrated,NativeJsonMigration.migrate(migrated));
        assertTrue(custom.getAsJsonObject().has("condition"));
    }
}
