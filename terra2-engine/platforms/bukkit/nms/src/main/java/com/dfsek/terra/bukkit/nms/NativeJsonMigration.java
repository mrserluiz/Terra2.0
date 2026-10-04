package com.dfsek.terra.bukkit.nms;

import com.google.gson.*;
import java.util.*;

/** Typed 1.21-era JSON adaptations to 26.2, applied to a detached execution copy. */
public final class NativeJsonMigration {
    private NativeJsonMigration() {}
    public static JsonElement migrate(JsonElement original) { return visit(original.deepCopy()); }
    private static String id(JsonObject object, String field) {
        if(!object.has(field) || !object.get(field).isJsonPrimitive()) return "";
        String value = object.get(field).getAsString(); return value.contains(":") ? value : "minecraft:" + value;
    }
    private static JsonElement visit(JsonElement json) {
        if(json.isJsonArray()) { var out = new JsonArray(); json.getAsJsonArray().forEach(value -> out.add(visit(value))); return out; }
        if(!json.isJsonObject()) return json;
        var object = json.getAsJsonObject();
        if(id(object, "condition").equals("minecraft:entity_properties") && object.has("predicate"))
            object.add("predicate", entity(object.getAsJsonObject("predicate")));
        if(id(object, "condition").equals("minecraft:time_check") && !object.has("clock"))
            object.addProperty("clock", "minecraft:overworld");
        if(Set.of("minecraft:random_patch", "minecraft:flower", "minecraft:flower_no_bonemeal").contains(id(object, "type")) && object.has("config"))
            object = patch(object);
        if(id(object, "type").equals("minecraft:matching_block_tag") && object.has("tag")) {
            Map<String, String> renamed = Map.of("minecraft:small_dripleaf_placeable", "minecraft:supports_small_dripleaf",
                "minecraft:big_dripleaf_placeable", "minecraft:supports_big_dripleaf");
            String tag = object.get("tag").getAsString(); if(renamed.containsKey(tag)) object.addProperty("tag", renamed.get(tag));
        }
        var result = new JsonObject(); object.entrySet().forEach(entry -> result.add(entry.getKey(), visit(entry.getValue()))); return result;
    }
    private static JsonObject entity(JsonObject old) {
        var out = new JsonObject();
        old.entrySet().forEach(entry -> {
            String key = entry.getKey(); var value = entry.getValue();
            if(key.equals("type") || key.equals("minecraft:type")) key = "minecraft:entity_type";
            if(key.equals("type_specific") || key.equals("minecraft:type_specific")) {
                var specific = value.getAsJsonObject().deepCopy(); String type = id(specific, "type"); specific.remove("type");
                String name = type.substring("minecraft:".length());
                if(!type.startsWith("minecraft:") || !Set.of("player", "lightning", "fishing_hook", "raider", "sheep", "slime").contains(name))
                    throw new IllegalArgumentException("Unsupported legacy entity sub-predicate: " + type);
                if(name.equals("slime")) name = "cube_mob";
                if(specific.has("looking_at")) specific.add("looking_at", entity(specific.getAsJsonObject("looking_at")));
                out.add("minecraft:type_specific/" + name, specific); return;
            }
            if(Set.of("vehicle", "passenger", "targeted_entity", "minecraft:vehicle", "minecraft:passenger", "minecraft:targeted_entity").contains(key) && value.isJsonObject())
                value = entity(value.getAsJsonObject());
            out.add(key, value);
        }); return out;
    }
    private static JsonObject patch(JsonObject old) {
        var config = old.getAsJsonObject("config");
        JsonElement feature = config.get("feature");
        JsonObject placed;
        if(feature.isJsonObject()) placed = feature.getAsJsonObject().deepCopy();
        else { placed = new JsonObject(); placed.add("feature", feature); placed.add("placement", new JsonArray()); }
        var modifiers = new JsonArray();
        var count = new JsonObject(); count.addProperty("type", "minecraft:count"); count.addProperty("count", config.has("tries") ? config.get("tries").getAsInt() : 128); modifiers.add(count);
        var offset = new JsonObject(); offset.addProperty("type", "minecraft:random_offset");
        offset.add("xz_spread", triangular(config.has("xz_spread") ? config.get("xz_spread").getAsInt() : 7));
        offset.add("y_spread", triangular(config.has("y_spread") ? config.get("y_spread").getAsInt() : 3)); modifiers.add(offset);
        if(placed.has("placement")) placed.getAsJsonArray("placement").forEach(modifiers::add);
        placed.add("placement", modifiers);
        var wrapper = new JsonObject(); wrapper.addProperty("type", "minecraft:random_selector");
        var result = new JsonObject(); result.add("features", new JsonArray()); result.add("default", placed); wrapper.add("config", result); return wrapper;
    }
    private static JsonElement triangular(int spread) {
        if(spread == 0) return new JsonPrimitive(0);
        var provider = new JsonObject(); provider.addProperty("type", "minecraft:trapezoid");
        provider.addProperty("min", -spread); provider.addProperty("max", spread); provider.addProperty("plateau", 0); return provider;
    }
}
