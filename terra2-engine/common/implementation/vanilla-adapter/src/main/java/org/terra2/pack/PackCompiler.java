package org.terra2.pack;

import com.google.gson.*;
import java.util.*;
import org.terra2.adapter.vanilla.FlatDefinition;

/** Supported definitions become neutral IR. Every unsupported semantic resource blocks READY status. */
public final class PackCompiler {
    public enum Profile { FULL, GENERATION_AND_LOOT }
    public record Result(FlatDefinition terrain, List<SimpleBlockFeature> features, List<String> blockers,
                         List<String> notes, Map<String, Integer> resourceKinds) {
        public Result { features = List.copyOf(features); blockers = List.copyOf(blockers); notes = List.copyOf(notes); resourceKinds = Map.copyOf(resourceKinds); }
        public boolean ready() { return blockers.isEmpty() && (terrain != null || !features.isEmpty()); }
    }
    private PackCompiler() {}
    public static Result compile(ResourceBundle source, String dimension) {
        return compile(source, dimension, Profile.FULL);
    }
    public static Result compile(ResourceBundle source, String dimension, Profile profile) {
        List<String> blockers = new ArrayList<>(), notes = new ArrayList<>();
        List<SimpleBlockFeature> features = new ArrayList<>();
        Map<String, Integer> kinds = new TreeMap<>();
        Set<String> handled = new HashSet<>();
        FlatDefinition terrain = null;
        blockers.addAll(metadataIssues(source));
        notes.add("Original metadata is preserved; its version is not changed to pretend native compatibility");
        List<String> dimensions = source.paths().stream().filter(path -> path.matches("data/[^/]+/dimension/.+\\.json")).toList();
        String selected = dimension;
        if(selected == null && dimensions.size() == 1) selected = idFor(dimensions.getFirst(), "dimension");
        if(selected == null && !dimensions.isEmpty()) blockers.add("Select one source dimension explicitly");
        if(selected != null) try {
            ResourceBundle.identifier(selected);
            String path = resource(selected, "dimension");
            terrain = flat(object(source.text(path)), selected, source.fingerprint()); handled.add(path);
            // Additional dimension definitions must be converted separately rather than discarded.
        } catch(RuntimeException error) { blockers.add("Terrain " + selected + ": " + error.getMessage()); }
        for(String path : source.paths()) {
            if(!path.matches("data/[^/]+/worldgen/placed_feature/.+\\.json")) continue;
            try {
                var placed = object(source.text(path)); fields(placed, Set.of("feature", "placement"));
                String reference = string(placed, "feature");
                String configuredPath = resource(ResourceBundle.identifier(reference), "worldgen/configured_feature");
                var configured = object(source.text(configuredPath));
                fields(configured, Set.of("type", "config"));
                if(!string(configured, "type").equals("minecraft:simple_block")) throw new IllegalArgumentException("Only minecraft:simple_block is supported");
                var config = child(configured, "config"); fields(config, Set.of("to_place", "schedule_tick"));
                if(config.has("schedule_tick") && bool(config, "schedule_tick")) throw new IllegalArgumentException("Scheduled ticks are unsupported");
                var provider = child(config, "to_place"); fields(provider, Set.of("type", "state"));
                if(!string(provider, "type").equals("minecraft:simple_state_provider")) throw new IllegalArgumentException("Only simple_state_provider is supported");
                var state = child(provider, "state"); fields(state, Set.of("Name", "Properties"));
                String block = ResourceBundle.identifier(string(state, "Name"));
                if(!block.startsWith("minecraft:")) throw new IllegalArgumentException("Custom block: " + block);
                if(state.has("Properties")) {
                    var properties = child(state, "Properties"); List<String> values = new ArrayList<>();
                    for(String key : new TreeSet<>(properties.keySet())) {
                        String value = string(properties, key);
                        if(!key.matches("[a-z0-9_]+") || !value.matches("[a-z0-9_]+")) throw new IllegalArgumentException("Invalid block property");
                        values.add(key + "=" + value);
                    }
                    if(!values.isEmpty()) block += "[" + String.join(",", values) + "]";
                }
                int count = 1, rarity = 1; boolean square = false, height = false;
                Set<String> modifiers = new HashSet<>();
                for(JsonElement entry : array(placed, "placement")) {
                    if(!entry.isJsonObject()) throw new IllegalArgumentException("Invalid placement modifier");
                    var modifier = entry.getAsJsonObject(); String type = string(modifier, "type");
                    if(!modifiers.add(type)) throw new IllegalArgumentException("Repeated placement modifier: " + type);
                    switch(type) {
                        case "minecraft:count" -> { fields(modifier, Set.of("type", "count")); count = integer(modifier.get("count")); }
                        case "minecraft:rarity_filter" -> { fields(modifier, Set.of("type", "chance")); rarity = integer(modifier.get("chance")); }
                        case "minecraft:in_square" -> { fields(modifier, Set.of("type")); square = true; }
                        case "minecraft:heightmap" -> {
                            fields(modifier, Set.of("type", "heightmap"));
                            if(!string(modifier, "heightmap").equals("WORLD_SURFACE_WG")) throw new IllegalArgumentException("Only WORLD_SURFACE_WG is supported");
                            height = true;
                        }
                        default -> throw new IllegalArgumentException("Unsupported placement modifier: " + type);
                    }
                }
                if(!square || !height || count < 1 || count > 64 || rarity < 1 || rarity > 1000000)
                    throw new IllegalArgumentException("Requires in_square/heightmap and count 1..64, chance 1..1000000");
                features.add(new SimpleBlockFeature(idFor(path, "worldgen/placed_feature"), block, count, rarity));
                handled.add(path); handled.add(configuredPath);
            } catch(RuntimeException error) { blockers.add(path + ": " + error.getMessage()); }
        }
        for(String path : source.paths()) {
            if(!path.startsWith("data/")) continue;
            String[] pieces = path.split("/", 4);
            String kind = pieces.length >= 3 ? pieces[2] : "invalid";
            if(kind.equals("worldgen") && pieces.length == 4) kind += "/" + pieces[3].split("/", 2)[0];
            kinds.merge(kind, 1, Integer::sum);
            if(profile == Profile.GENERATION_AND_LOOT && excludedGameplay(path)) {
                notes.add("Excluded from GENERATION_AND_LOOT profile (original preserved): " + path);
                continue;
            }
            if(!handled.contains(path)) blockers.add("Unsupported/unconsumed resource: " + path);
        }
        if(features.size() > 32 || features.stream().mapToInt(SimpleBlockFeature::count).sum() > 128)
            blockers.add("Feature budget exceeded: 32 features / 128 attempts per chunk");
        if(terrain == null && features.isEmpty()) blockers.add("No executable terrain or features compiled");
        if(!features.isEmpty()) notes.add("Features use Terra2's deterministic sampler; vanilla placement seed parity/survival rules are not claimed");
        features.sort(Comparator.comparing(SimpleBlockFeature::id));
        return new Result(terrain, features, blockers, notes, kinds);
    }
    public static boolean excludedGameplay(String path) {
        return path.matches("data/[^/]+/(function|advancement|recipe)/.+")
            || path.matches("data/[^/]+/tags/function/.+");
    }
    public static List<String> metadataIssues(ResourceBundle source) {
        List<String> issues = new ArrayList<>();
        if(!source.contains("pack.mcmeta")) return List.of("Missing pack.mcmeta at source root");
        try {
            var metadata = object(source.text("pack.mcmeta"));
            var pack = child(metadata, "pack");
            if(!pack.has("description")) issues.add("Missing pack.description");
            for(String field : metadata.keySet()) if(!Set.of("pack", "overlays").contains(field)) issues.add("Unsupported metadata: " + field);
            if(metadata.has("overlays")) {
                var overlays = child(metadata, "overlays"); fields(overlays, Set.of("entries"));
                if(!array(overlays, "entries").isEmpty()) issues.add("Version-dependent overlays require semantic migration");
            }
        } catch(RuntimeException error) { issues.add("Invalid metadata: " + error.getMessage()); }
        return List.copyOf(issues);
    }
    private static FlatDefinition flat(JsonObject root, String id, String fingerprint) {
        fields(root, Set.of("type", "generator"));
        if(!string(root, "type").equals("minecraft:overworld")) throw new IllegalArgumentException("Custom/nether dimension type is unsupported");
        var generator = child(root, "generator"); fields(generator, Set.of("type", "settings"));
        if(!string(generator, "type").equals("minecraft:flat")) throw new IllegalArgumentException("Noise/density/surface compilation is pending");
        var settings = child(generator, "settings"); fields(settings, Set.of("biome", "layers", "features", "lakes", "structure_overrides"));
        for(String flag : List.of("features", "lakes")) if(settings.has(flag) && bool(settings, flag))
            throw new IllegalArgumentException("Flat " + flag + " must be false");
        if(!settings.has("structure_overrides") || !array(settings, "structure_overrides").isEmpty())
            throw new IllegalArgumentException("structure_overrides must be []; jigsaw/structure stages pending");
        String biome = ResourceBundle.identifier(string(settings, "biome"));
        if(!biome.startsWith("minecraft:")) throw new IllegalArgumentException("Custom biomes pending");
        List<FlatDefinition.Layer> layers = new ArrayList<>(); int total = 0;
        for(var entry : array(settings, "layers")) {
            var layer = entry.getAsJsonObject(); fields(layer, Set.of("height", "block"));
            int height = integer(layer.get("height")); String block = ResourceBundle.identifier(string(layer, "block"));
            if(height < 0 || height > 384 || (total += height) > 384 || !block.startsWith("minecraft:")) throw new IllegalArgumentException("Invalid flat layer");
            layers.add(new FlatDefinition.Layer(height, block));
        }
        if(layers.size() > 384 || total < 1) throw new IllegalArgumentException("Invalid flat layer count/height");
        return new FlatDefinition(id, biome, layers, fingerprint);
    }
    public static JsonObject object(String text) {
        JsonElement value = JsonParser.parseString(text);
        if(!value.isJsonObject()) throw new IllegalArgumentException("Expected JSON object");
        return value.getAsJsonObject();
    }
    static JsonObject child(JsonObject parent, String key) {
        var value = parent.get(key); if(value == null || !value.isJsonObject()) throw new IllegalArgumentException("Expected object: " + key);
        return value.getAsJsonObject();
    }
    static JsonArray array(JsonObject parent, String key) {
        var value = parent.get(key); if(value == null || !value.isJsonArray()) throw new IllegalArgumentException("Expected array: " + key);
        return value.getAsJsonArray();
    }
    static String string(JsonObject parent, String key) {
        var value = parent.get(key); if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new IllegalArgumentException("Expected string: " + key);
        return value.getAsString();
    }
    static boolean bool(JsonObject parent, String key) {
        var value = parent.get(key); if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException("Expected boolean: " + key);
        return value.getAsBoolean();
    }
    static int integer(JsonElement value) {
        if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Expected integer");
        return value.getAsBigDecimal().intValueExact();
    }
    static void fields(JsonObject value, Set<String> allowed) {
        for(String field : value.keySet()) if(!allowed.contains(field)) throw new IllegalArgumentException("Unsupported field: " + field);
    }
    static String resource(String id, String type) {
        String[] parts = id.split(":", 2); return "data/" + parts[0] + "/" + type + "/" + parts[1] + ".json";
    }
    static String idFor(String path, String type) {
        String namespace = path.split("/")[1]; String prefix = "data/" + namespace + "/" + type + "/";
        return ResourceBundle.identifier(namespace + ":" + path.substring(prefix.length(), path.length() - 5));
    }
}
