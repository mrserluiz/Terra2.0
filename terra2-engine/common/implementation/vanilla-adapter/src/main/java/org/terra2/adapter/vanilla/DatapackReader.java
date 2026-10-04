package org.terra2.adapter.vanilla;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/** Read-only importer. It never extracts archives or installs resources into server registries. */
public final class DatapackReader {
    private static final int MAX_FILE = 1024 * 1024, MAX_TOTAL = 32 * 1024 * 1024, MAX_FILES = 4096;
    public record Inspection(String fingerprint, Set<String> dimensions, Set<String> unsupported) {
        public Inspection { dimensions = Set.copyOf(dimensions); unsupported = Set.copyOf(unsupported); }
    }
    private final SortedMap<String, byte[]> files;
    private final String fingerprint;
    private DatapackReader(SortedMap<String, byte[]> files) {
        this.files = files;
        try {
            var hash = MessageDigest.getInstance("SHA-256");
            files.forEach((name, bytes) -> { hash.update(name.getBytes(StandardCharsets.UTF_8)); hash.update((byte) 0); hash.update(bytes); });
            fingerprint = HexFormat.of().formatHex(hash.digest());
        } catch(NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
        var metadata = object("pack.mcmeta");
        fields(metadata, Set.of("pack"), "pack.mcmeta");
        var pack = child(metadata, "pack");
        fields(pack, Set.of("description", "pack_format", "supported_formats", "min_format", "max_format"), "pack");
        // Paper 26.2 / Minecraft 26.2 has data format 107.1. No implicit version upgrade.
        int[] min, max;
        if(pack.has("min_format") && pack.has("max_format")) {
            min = version(pack.get("min_format")); max = version(pack.get("max_format"));
        } else if(pack.has("supported_formats")) {
            JsonElement value = pack.get("supported_formats");
            if(value.isJsonObject()) {
                min = version(value.getAsJsonObject().get("min_inclusive"));
                max = version(value.getAsJsonObject().get("max_inclusive"));
                max[1] = Integer.MAX_VALUE;
            } else if(value.isJsonArray() && value.getAsJsonArray().size() == 2) {
                min = version(value.getAsJsonArray().get(0)); max = version(value.getAsJsonArray().get(1)); max[1] = Integer.MAX_VALUE;
            } else { min = version(value); max = min.clone(); max[1] = Integer.MAX_VALUE; }
        } else {
            min = version(pack.get("pack_format")); max = min.clone(); max[1] = Integer.MAX_VALUE;
        }
        if(compare(min, max) > 0 || compare(min, new int[]{107, 1}) > 0 || compare(max, new int[]{107, 1}) < 0)
            throw new IllegalArgumentException("Datapack format does not support Minecraft 26.2 (107.1)");
    }
    public static DatapackReader open(Path root) throws IOException {
        if(Files.isSymbolicLink(root)) throw new IOException("Symbolic links are not supported: " + root);
        SortedMap<String, byte[]> files = new TreeMap<>();
        if(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            try(var paths = Files.walk(root)) {
                Iterator<Path> iterator = paths.iterator();
                while(iterator.hasNext()) {
                    Path file = iterator.next();
                    if(Files.isSymbolicLink(file)) throw new IOException("Symbolic link in datapack: " + file);
                    if(!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) continue;
                    try(var input = Files.newInputStream(file)) { add(files, root.relativize(file).toString().replace('\\', '/'), input); }
                }
            }
        } else {
            try(var zip = new ZipFile(root.toFile())) {
                var entries = zip.entries();
                int count = 0;
                while(entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if(++count > MAX_FILES) throw new IOException("Too many archive entries");
                    path(entry.getName().replaceFirst("/$", ""));
                    if(entry.isDirectory()) continue;
                    try(var input = zip.getInputStream(entry)) { add(files, entry.getName(), input); }
                }
            }
        }
        return new DatapackReader(files);
    }
    private static void add(SortedMap<String, byte[]> files, String name, InputStream input) throws IOException {
        path(name);
        if(files.containsKey(name)) throw new IOException("Duplicate datapack resource: " + name);
        if(files.size() >= MAX_FILES) throw new IOException("Too many datapack files");
        byte[] bytes = input.readNBytes(MAX_FILE + 1);
        if(bytes.length > MAX_FILE) throw new IOException("Resource exceeds 1 MiB: " + name);
        if(files.values().stream().mapToLong(value -> value.length).sum() + bytes.length > MAX_TOTAL)
            throw new IOException("Datapack exceeds 32 MiB");
        files.put(name, bytes);
    }
    private static void path(String path) {
        if(path.isBlank() || path.startsWith("/") || path.contains("\\") || path.contains(":"))
            throw new IllegalArgumentException("Invalid resource path: " + path);
        for(String part : path.split("/", -1)) if(part.isBlank() || part.equals(".") || part.equals(".."))
            throw new IllegalArgumentException("Invalid resource path: " + path);
    }
    public Inspection inspect() {
        Set<String> dimensions = new TreeSet<>(), unsupported = new TreeSet<>();
        for(String file : files.keySet()) {
            if(file.equals("pack.mcmeta") || file.equals("pack.png")) continue;
            String[] parts = file.split("/", 4);
            if(parts.length == 4 && parts[0].equals("data") && parts[2].equals("dimension") && file.endsWith(".json"))
                dimensions.add(identifier(parts[1] + ":" + parts[3].substring(0, parts[3].length() - 5)));
            else unsupported.add(file);
        }
        return new Inspection(fingerprint, dimensions, unsupported);
    }
    public FlatDefinition flat(String dimension) {
        identifier(dimension);
        Inspection inspection = inspect();
        if(!inspection.unsupported().isEmpty()) throw new IllegalArgumentException("Unsupported datapack resources: " + inspection.unsupported());
        String[] id = dimension.split(":", 2);
        var definition = object("data/" + id[0] + "/dimension/" + id[1] + ".json");
        fields(definition, Set.of("type", "generator"), dimension);
        if(!string(definition, "type").equals("minecraft:overworld"))
            throw new IllegalArgumentException("Only the minecraft:overworld dimension type is supported");
        var generator = child(definition, "generator");
        fields(generator, Set.of("type", "settings"), "generator");
        if(!string(generator, "type").equals("minecraft:flat"))
            throw new IllegalArgumentException("Unsupported generator: " + string(generator, "type") + "; first adapter supports minecraft:flat");
        var settings = child(generator, "settings");
        fields(settings, Set.of("biome", "layers", "features", "lakes", "structure_overrides"), "flat.settings");
        for(String flag : List.of("features", "lakes")) if(settings.has(flag)) {
            JsonElement value = settings.get(flag);
            if(!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean() || value.getAsBoolean())
                throw new IllegalArgumentException(flag + " must be false; feature conversion is not implemented");
        }
        if(!settings.has("structure_overrides") || !settings.get("structure_overrides").isJsonArray()
                || !settings.getAsJsonArray("structure_overrides").isEmpty())
            throw new IllegalArgumentException("structure_overrides must be []; structure conversion is not implemented");
        String biome = identifier(string(settings, "biome"));
        if(!biome.startsWith("minecraft:")) throw new IllegalArgumentException("Custom biomes are not supported: " + biome);
        JsonElement value = settings.get("layers");
        if(value == null || !value.isJsonArray() || value.getAsJsonArray().isEmpty() || value.getAsJsonArray().size() > 384)
            throw new IllegalArgumentException("layers must contain 1..384 layers");
        List<FlatDefinition.Layer> layers = new ArrayList<>();
        int total = 0;
        for(JsonElement item : value.getAsJsonArray()) {
            if(!item.isJsonObject()) throw new IllegalArgumentException("Layer must be an object");
            var layer = item.getAsJsonObject(); fields(layer, Set.of("height", "block"), "layer");
            int height = integer(layer.get("height"));
            if(height < 0 || height > 384 || (total += height) > 384) throw new IllegalArgumentException("Flat layer height outside 0..384");
            String block = identifier(string(layer, "block"));
            if(!block.startsWith("minecraft:")) throw new IllegalArgumentException("Custom block is not supported: " + block);
            layers.add(new FlatDefinition.Layer(height, block));
        }
        if(total == 0) throw new IllegalArgumentException("Flat definition has no terrain");
        return new FlatDefinition(dimension, biome, layers, fingerprint);
    }
    private JsonObject object(String resource) {
        byte[] bytes = files.get(resource);
        if(bytes == null) throw new IllegalArgumentException("Missing resource: " + resource);
        try {
            JsonElement json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
            if(!json.isJsonObject()) throw new IllegalArgumentException("Expected JSON object: " + resource);
            return json.getAsJsonObject();
        } catch(JsonParseException error) { throw new IllegalArgumentException("Invalid JSON: " + resource, error); }
    }
    private static JsonObject child(JsonObject parent, String key) {
        JsonElement value = parent.get(key);
        if(value == null || !value.isJsonObject()) throw new IllegalArgumentException("Expected object: " + key);
        return value.getAsJsonObject();
    }
    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
            throw new IllegalArgumentException("Expected string: " + key);
        return value.getAsString();
    }
    private static void fields(JsonObject object, Set<String> allowed, String where) {
        for(String field : object.keySet()) if(!allowed.contains(field))
            throw new IllegalArgumentException("Unsupported field " + where + "." + field);
    }
    private static String identifier(String id) {
        if(!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || Arrays.stream(id.split("[:/]", -1)).anyMatch(part -> part.equals("..") || part.equals(".") || part.isBlank()))
            throw new IllegalArgumentException("Invalid namespaced identifier: " + id);
        return id;
    }
    private static int integer(JsonElement value) {
        if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
            throw new IllegalArgumentException("Expected integer");
        try { return value.getAsBigDecimal().intValueExact(); }
        catch(ArithmeticException error) { throw new IllegalArgumentException("Expected integer", error); }
    }
    private static int[] version(JsonElement value) {
        if(value != null && value.isJsonArray() && value.getAsJsonArray().size() == 2)
            return new int[]{integer(value.getAsJsonArray().get(0)), integer(value.getAsJsonArray().get(1))};
        return new int[]{integer(value), 0};
    }
    private static int compare(int[] left, int[] right) {
        int major = Integer.compare(left[0], right[0]); return major != 0 ? major : Integer.compare(left[1], right[1]);
    }
}
