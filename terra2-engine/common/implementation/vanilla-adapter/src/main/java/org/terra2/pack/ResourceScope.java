package org.terra2.pack;

import com.google.gson.*;
import java.util.*;

/** Stable private identifiers. Codec types and jigsaw connector labels are never resource aliases. */
public final class ResourceScope {
    private static final Set<String> CODEC_FIELDS = Set.of("type", "function", "condition", "processor_type", "predicate_type", "element_type", "start_jigsaw_name",
        "item_model", "minecraft:item_model", "asset_id", "texture", "translate");
    private final String fingerprint;
    private final Set<String> resources = new HashSet<>(), tags = new HashSet<>();
    public ResourceScope(ResourceBundle source) {
        fingerprint = source.fingerprint();
        for(String path : source.paths()) {
            if(PackCompiler.excludedGameplay(path)) continue;
            if(!path.startsWith("data/") || !(path.endsWith(".json") || path.endsWith(".nbt"))) continue;
            String[] parts = path.split("/", 4); if(parts.length != 4) continue;
            String tail = parts[3];
            boolean tag = parts[2].equals("tags");
            if(tag && tail.startsWith("worldgen/")) tail = tail.substring("worldgen/".length());
            if(parts[2].equals("worldgen") || tag) {
                int slash = tail.indexOf('/'); if(slash < 0) continue;
                tail = tail.substring(slash + 1);
            }
            String id = parts[1] + ":" + tail.substring(0, tail.lastIndexOf('.'));
            (tag ? tags : resources).add(ResourceBundle.identifier(id));
        }
    }
    public String fingerprint() { return fingerprint; }
    public String privateId(String original) {
        String id = qualified(original); String[] parts = id.split(":", 2);
        return "terra2:" + fingerprint + "/" + parts[0] + "/" + parts[1];
    }
    public String resource(String original) {
        String id = qualified(original); return resources.contains(id) ? privateId(id) : original;
    }
    public String tag(String original) {
        boolean prefix = original.startsWith("#"); String id = qualified(prefix ? original.substring(1) : original);
        return tags.contains(id) ? (prefix ? "#" : "") + privateId(id) : original;
    }
    private static String qualified(String id) { return ResourceBundle.identifier(id.contains(":") ? id : "minecraft:" + id); }
    public JsonElement rewrite(JsonElement source) { return rewrite(source, ""); }
    private JsonElement rewrite(JsonElement source, String field) {
        if(source.isJsonObject()) {
            var result = new JsonObject();
            source.getAsJsonObject().entrySet().forEach(entry -> {
                String key = entry.getKey();
                if(key.contains(":") && resources.contains(key)) key = privateId(key);
                result.add(key, rewrite(entry.getValue(), entry.getKey()));
            });
            return result;
        }
        if(source.isJsonArray()) {
            var result = new JsonArray(); source.getAsJsonArray().forEach(entry -> result.add(rewrite(entry, field))); return result;
        }
        if(!source.isJsonPrimitive() || !source.getAsJsonPrimitive().isString() || CODEC_FIELDS.contains(field)) return source.deepCopy();
        String value = source.getAsString();
        if(!value.matches("#?[a-z0-9_.-]+:[a-z0-9_./-]+") && !value.matches("[a-z0-9_./-]+")) return source.deepCopy();
        if(value.startsWith("#") || field.equals("tag") || field.equals("destination")) return new JsonPrimitive(tag(value));
        return new JsonPrimitive(resource(value));
    }
}
