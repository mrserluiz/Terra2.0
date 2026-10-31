package com.dfsek.terra.bukkit.nms;

import com.google.gson.*;
import java.util.*;

/** Typed legacy JSON adaptations to 26.3, applied to a detached execution copy. */
public final class NativeJsonMigration {
    private static final Map<String,String> PROVIDERS=Map.of("simple_state_provider","simple","weighted_state_provider","weighted",
            "noise_provider","noise","noise_threshold_provider","noise_threshold","dual_noise_provider","dual_noise",
            "rotated_block_provider","rotated","randomized_int_state_provider","randomized_int");
    private NativeJsonMigration() {}
    public static JsonElement migrate(JsonElement original) { return visit(original.deepCopy(), "", ""); }
    public static JsonElement migrate(JsonElement original,String category) {
        String field=category.equals("item_modifier")?"modifier":category.equals("predicate")?"condition":"";
        return visit(original.deepCopy(),field,"");
    }
    private static String id(JsonObject object, String field) {
        if(!object.has(field) || !object.get(field).isJsonPrimitive()) return "";
        String value = object.get(field).getAsString(); return value.contains(":") ? value : "minecraft:" + value;
    }
    private static JsonElement visit(JsonElement json,String field,String parentType) {
        if(Set.of("minecraft:custom_data","custom_data","nbt").contains(field))return json;
        if(json.isJsonArray()) {
            var out = new JsonArray();json.getAsJsonArray().forEach(value -> out.add(visit(value,"",parentType)));
            if(Set.of("condition","requirements","modifier").contains(field)) {
                var wrapper=new JsonObject();boolean modifier=field.equals("modifier");
                wrapper.addProperty("type",modifier?"minecraft:sequence":"minecraft:all_of");
                wrapper.add(modifier?"functions":"terms",out);return wrapper;
            }
            return out;
        }
        if(!json.isJsonObject()) return json;
        var object = json.getAsJsonObject();
        // Legacy serialized block states occur in processors, state providers and features.
        if(object.has("Name") && object.keySet().stream().allMatch(key -> key.equals("Name") || key.equals("Properties"))) {
            rename(object,"Name","id");rename(object,"Properties","properties");
        }
        String objectType=object.has("type")?id(object,"type"):object.has("function")?id(object,"function"):id(object,"condition");
        if(objectType.equals("minecraft:reference") && (object.has("function") || object.has("condition")) && object.has("name")) {
            var reference=object.get("name").deepCopy();
            if(object.has("function") && object.has("conditions") && !object.getAsJsonArray("conditions").isEmpty()) {
                var sequence=new JsonObject();sequence.addProperty("type","minecraft:sequence");
                var functions=new JsonArray();functions.add(reference);sequence.add("functions",functions);
                sequence.add("condition",object.get("conditions"));return visit(sequence,field,parentType);
            }
            return reference;
        }
        if(object.keySet().equals(Set.of("min","max")) && (Set.of("rolls","bonus_rolls").contains(field)
            || Set.of("minecraft:set_count","minecraft:set_damage","minecraft:enchant_with_levels","minecraft:enchant_randomly",
                "minecraft:set_ominous_bottle_amplifier").contains(parentType)))object.addProperty("type","minecraft:uniform");
        if(object.has("minCount") && object.has("maxCount") && object.has("type") && !object.has("count")) {
            var count=new JsonObject();count.addProperty("type","minecraft:uniform");
            count.add("min_inclusive",object.remove("minCount"));count.add("max_inclusive",object.remove("maxCount"));object.add("count",count);
        }
        String provider=PROVIDERS.get(objectType.replace("minecraft:",""));
        if(provider!=null)object.addProperty("type","minecraft:"+provider);
        if(objectType.equals("minecraft:tag") && object.has("name") && !object.has("items")) {
            object.addProperty("items","#"+object.remove("name").getAsString());
        }
        if(objectType.equals("minecraft:exploration_map") && object.has("function") && !object.has("type")) {
            String destination=object.has("destination")?object.get("destination").getAsString():"minecraft:on_treasure_maps";
            destination=switch(destination) {
                case "minecraft:on_woodland_explorer_maps" -> "minecraft:on_woodland_mansion_maps";
                case "minecraft:on_ocean_explorer_maps" -> "minecraft:on_ocean_monument_maps";
                case "minecraft:on_trial_chambers_maps" -> "minecraft:on_buried_trial_chambers_maps";
                default -> destination;
            };
            if(!destination.startsWith("#"))object.addProperty("destination","#"+destination);
        }
        if(objectType.equals("minecraft:limit_count") && object.has("limit") && object.get("limit").isJsonPrimitive()) {
            var limit=new JsonObject();limit.add("min",object.get("limit"));limit.add("max",object.get("limit").deepCopy());object.add("limit",limit);
        }
        if(objectType.equals("minecraft:random_offset")) {
            object.addProperty("type","minecraft:offset");
            JsonElement spread=object.has("xz_spread")?object.remove("xz_spread"):new JsonPrimitive(0);
            object.add("x",spread);object.add("z",spread.deepCopy());
            if(object.has("y_spread"))object.add("y",object.remove("y_spread"));
        }
        if(objectType.equals("minecraft:damage_source_properties") && object.has("condition") && !object.has("type") && object.has("predicate")) {
            var predicate=object.getAsJsonObject("predicate");
            if(predicate.has("tags"))for(var entry:predicate.getAsJsonArray("tags")) {
                var tag=entry.getAsJsonObject();String value=tag.get("id").getAsString();
                if(!value.startsWith("#"))tag.addProperty("id","#"+value);
            }
        }
        // Vanilla 26.3 removes map_color too. Preserve provenance as inert custom data.
        if(field.equals("components") && object.has("minecraft:map_color")) {
            var color=object.remove("minecraft:map_color");
            if(!object.has("minecraft:custom_data"))object.add("minecraft:custom_data",new JsonObject());
            if(object.get("minecraft:custom_data").isJsonObject())
                object.getAsJsonObject("minecraft:custom_data").add("terra2:legacy_map_color",color);
        }
        String densityType=id(object,"type");
        if(densityType.equals("minecraft:y_clamped_gradient")) {
            object.addProperty("type","minecraft:gradient");object.addProperty("axis","y");
            rename(object,"from_y","from_coordinate");rename(object,"to_y","to_coordinate");
        }
        if(Set.of("minecraft:add","minecraft:mul","minecraft:min","minecraft:max").contains(densityType)) {
            rename(object,"argument1","left");rename(object,"argument2","right");
        }
        if(Set.of("minecraft:blend_density","minecraft:abs","minecraft:square","minecraft:cube","minecraft:half_negative",
            "minecraft:quarter_negative","minecraft:squeeze","minecraft:clamp","minecraft:interpolated").contains(densityType))
            rename(object,"argument","input");
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
        if(Set.of("minecraft:vegetation_patch", "minecraft:waterlogged_vegetation_patch").contains(id(object, "type")) && object.has("config")) {
            var config = object.getAsJsonObject("config");
            if(config.has("replaceable") && config.get("replaceable").isJsonPrimitive()
                    && config.get("replaceable").getAsString().equals("#minecraft:small_dripleaf_placeable"))
                config.addProperty("replaceable", "#minecraft:supports_small_dripleaf");
        }
        if(object.has("type") && object.has("config") && object.get("config").isJsonObject()) {
            var config=object.remove("config").getAsJsonObject();
            for(var entry:config.entrySet()) {
                if(object.has(entry.getKey()))throw new IllegalArgumentException("Conflicting legacy feature config field: "+entry.getKey());
                object.add(entry.getKey(),entry.getValue());
            }
        }
        // 26.3 loot codecs use typed conditions/modifiers; inline lists retain all/sequence semantics.
        String function=object.has("function") ? id(object,"function") : id(object,"type");
        if(object.has("function"))rename(object,"function","type");
        // Modern untyped loot pools can reference a predicate directly in "condition".
        // That is not the old discriminator and must survive repeated migration.
        if(!object.has("entries") && !object.has("pools") && !object.has("rolls")
            && object.has("condition") && object.get("condition").isJsonPrimitive())rename(object,"condition","type");
        if(!function.equals("minecraft:sequence"))rename(object,"functions","modifier");
        rename(object,"conditions","condition");
        var result = new JsonObject(); object.entrySet().forEach(entry -> result.add(entry.getKey(), visit(entry.getValue(),entry.getKey(),objectType))); return result;
    }
    private static void rename(JsonObject object,String old,String modern) {
        if(object.has(old) && !object.has(modern))object.add(modern,object.remove(old));
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
