package org.terra2.pack;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Bounded, platform-independent reader. Decoding a template never places blocks or executes its NBT. */
public final class StructureNbt {
    private static final int MAX_BYTES = 16 * 1024 * 1024, MAX_NODES = 2000000, MAX_ELEMENTS = 1000000;
    public record Position(int x, int y, int z) {}
    public record State(String name, Map<String, String> properties) {}
    public record Block(Position position, int state, Map<String, Object> nbt) {}
    public record Template(int dataVersion, Position size, List<List<State>> palettes, List<Block> blocks,
                           List<Map<String, Object>> entities) {}
    private final DataInputStream input;
    private int nodes;
    private StructureNbt(InputStream input) { this.input = new DataInputStream(input); }
    public static Template read(byte[] bytes) throws IOException {
        InputStream raw = new ByteArrayInputStream(bytes);
        if(bytes.length >= 2 && (bytes[0] & 255) == 31 && (bytes[1] & 255) == 139) raw = new GZIPInputStream(raw);
        try(var stream = raw) {
            byte[] decoded = stream.readNBytes(MAX_BYTES + 1);
            if(decoded.length > MAX_BYTES) throw new IOException("Structure NBT exceeds 16 MiB decompressed");
            var reader = new StructureNbt(new ByteArrayInputStream(decoded));
            if(reader.input.readUnsignedByte() != 10) throw new IOException("Structure NBT root must be a compound");
            reader.input.readUTF();
            var root = compound(reader.payload(10, 0));
            if(reader.input.read() != -1) throw new IOException("Trailing structure NBT data");
            return template(root);
        } catch(IllegalArgumentException | ClassCastException error) { throw new IOException("Invalid structure NBT: " + error.getMessage(), error); }
    }
    private Object payload(int tag, int depth) throws IOException {
        if(depth > 64 || ++nodes > MAX_NODES) throw new IOException("NBT depth/node budget exceeded");
        return switch(tag) {
            case 1 -> input.readByte(); case 2 -> input.readShort(); case 3 -> input.readInt(); case 4 -> input.readLong();
            case 5 -> input.readFloat(); case 6 -> input.readDouble();
            case 7 -> { int n = length(1); byte[] values = new byte[n]; input.readFully(values); yield values; }
            case 8 -> input.readUTF();
            case 9 -> {
                int type = input.readUnsignedByte(), n = length(1);
                if(type > 12 || (type == 0 && n != 0)) throw new IOException("Invalid NBT list type");
                List<Object> values = new ArrayList<>(n);
                for(int i = 0; i < n; i++) values.add(payload(type, depth + 1));
                yield List.copyOf(values);
            }
            case 10 -> {
                Map<String, Object> values = new LinkedHashMap<>();
                while(true) {
                    int type = input.readUnsignedByte(); if(type == 0) break;
                    String key = input.readUTF();
                    if(values.containsKey(key)) throw new IOException("Duplicate NBT field: " + key);
                    values.put(key, payload(type, depth + 1));
                }
                yield Collections.unmodifiableMap(values);
            }
            case 11 -> { int n = length(4); int[] values = new int[n]; for(int i = 0; i < n; i++) values[i] = input.readInt(); yield values; }
            case 12 -> { int n = length(8); long[] values = new long[n]; for(int i = 0; i < n; i++) values[i] = input.readLong(); yield values; }
            default -> throw new IOException("Unknown NBT tag: " + tag);
        };
    }
    private int length(int width) throws IOException {
        int n = input.readInt();
        if(n < 0 || n > MAX_ELEMENTS || n > MAX_NODES - nodes || (long) n * width > input.available()) throw new IOException("Invalid/oversized NBT array or list");
        nodes += n; return n;
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> compound(Object value) {
        if(!(value instanceof Map<?, ?>)) throw new IllegalArgumentException("Expected compound");
        return (Map<String, Object>) value;
    }
    private static List<?> list(Object value) {
        if(!(value instanceof List<?> result)) throw new IllegalArgumentException("Expected list"); return result;
    }
    private static int integer(Object value) {
        if(!(value instanceof Integer result)) throw new IllegalArgumentException("Expected TAG_Int"); return result;
    }
    private static Position position(Object value) {
        var values = list(value); if(values.size() != 3) throw new IllegalArgumentException("Expected 3D position");
        return new Position(integer(values.get(0)), integer(values.get(1)), integer(values.get(2)));
    }
    private static Template template(Map<String, Object> root) {
        int version = integer(root.get("DataVersion")); if(version < 0) throw new IllegalArgumentException("Negative DataVersion");
        Position size = position(root.get("size"));
        if(size.x < 1 || size.y < 1 || size.z < 1 || size.x > 512 || size.y > 512 || size.z > 512
                || (long) size.x * size.y * size.z > MAX_ELEMENTS) throw new IllegalArgumentException("Template size exceeds budget");
        List<List<State>> palettes = new ArrayList<>();
        if(root.containsKey("palette") == root.containsKey("palettes")) throw new IllegalArgumentException("Requires exactly one palette or palettes");
        var rawPalettes = root.containsKey("palette") ? List.of(root.get("palette")) : list(root.get("palettes"));
        if(rawPalettes.isEmpty() || rawPalettes.size() > 64) throw new IllegalArgumentException("Invalid palette count");
        for(Object rawPalette : rawPalettes) {
            List<State> states = new ArrayList<>();
            for(Object rawState : list(rawPalette)) {
                var state = compound(rawState); Object name = state.get("Name");
                if(!(name instanceof String)) throw new IllegalArgumentException("Missing block Name");
                ResourceBundle.identifier((String) name);
                Map<String, String> properties = new TreeMap<>();
                if(state.containsKey("Properties")) compound(state.get("Properties")).forEach((key, value) -> {
                    if(!(value instanceof String text) || !key.matches("[a-z0-9_]+") || !text.matches("[a-z0-9_]+"))
                        throw new IllegalArgumentException("Invalid block property");
                    properties.put(key, text);
                });
                states.add(new State((String) name, Collections.unmodifiableMap(properties)));
            }
            // Empty templates are valid no-op pool elements (including vanilla structure removers).
            // Any block referencing an empty palette is still rejected below.
            if(states.size() > 65536) throw new IllegalArgumentException("Invalid palette size");
            if(!palettes.isEmpty() && palettes.getFirst().size() != states.size()) throw new IllegalArgumentException("Palette sizes differ");
            palettes.add(List.copyOf(states));
        }
        List<Block> blocks = new ArrayList<>(); Set<Position> seen = new HashSet<>();
        for(Object rawBlock : list(root.get("blocks"))) {
            var block = compound(rawBlock); Position pos = position(block.get("pos")); int state = integer(block.get("state"));
            if(pos.x < 0 || pos.y < 0 || pos.z < 0 || pos.x >= size.x || pos.y >= size.y || pos.z >= size.z)
                throw new IllegalArgumentException("Block outside template");
            if(!seen.add(pos)) throw new IllegalArgumentException("Duplicate block position");
            if(state < 0 || state >= palettes.getFirst().size()) throw new IllegalArgumentException("Block palette index outside range");
            blocks.add(new Block(pos, state, block.containsKey("nbt") ? compound(block.get("nbt")) : Map.of()));
        }
        List<Map<String, Object>> entities = new ArrayList<>();
        for(Object entity : list(root.getOrDefault("entities", List.of()))) entities.add(compound(entity));
        return new Template(version, size, List.copyOf(palettes), List.copyOf(blocks), List.copyOf(entities));
    }
}
