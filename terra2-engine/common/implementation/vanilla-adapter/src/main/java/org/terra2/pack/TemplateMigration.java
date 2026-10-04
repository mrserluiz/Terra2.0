package org.terra2.pack;

import java.io.IOException;
import java.util.*;

/** Native data fixing is separate from authorization to generate structures. */
public final class TemplateMigration {
    public interface Backend {
        int targetDataVersion();
        byte[] migrate(byte[] source) throws IOException;
    }
    public record Piece(String path, int sourceDataVersion, int targetDataVersion,
                        int blocks, int entities, int jigsaws) {}
    public record Report(String status, Integer targetDataVersion, List<Piece> pieces, List<String> errors) {
        public Report { pieces = List.copyOf(pieces); errors = List.copyOf(errors); }
    }
    public record Result(Report report, Map<String, byte[]> templates) {
        public Result {
            Map<String, byte[]> copy = new TreeMap<>();
            templates.forEach((path, bytes) -> copy.put(path, bytes.clone()));
            templates = Collections.unmodifiableMap(copy);
        }
        @Override public Map<String, byte[]> templates() {
            Map<String, byte[]> copy = new TreeMap<>();
            templates.forEach((path, bytes) -> copy.put(path, bytes.clone()));
            return Collections.unmodifiableMap(copy);
        }
    }
    private TemplateMigration() {}
    public static Result run(ResourceBundle source, Backend backend) {
        var paths = source.paths().stream().filter(path -> path.matches("data/[^/]+/structure/.+\\.nbt")).toList();
        if(paths.isEmpty()) return new Result(new Report("NOT_REQUIRED", null, List.of(), List.of()), Map.of());
        if(backend == null) return new Result(new Report("PENDING", null, List.of(), List.of("Native DataFixer backend unavailable")), Map.of());
        int target = backend.targetDataVersion();
        if(target < 1) throw new IllegalArgumentException("Invalid native target DataVersion");
        var pieces = new ArrayList<Piece>(); var errors = new ArrayList<String>();
        Map<String, byte[]> migrated = new TreeMap<>(); long total = 0;
        for(String path : paths) try {
            byte[] originalBytes = source.bytes(path);
            var original = StructureNbt.read(originalBytes);
            if(original.dataVersion() < 0 || original.dataVersion() > target)
                throw new IOException("Source DataVersion " + original.dataVersion() + " cannot be migrated to " + target);
            byte[] bytes = Objects.requireNonNull(backend.migrate(originalBytes), "Native migration returned null");
            total += bytes.length;
            if(bytes.length > 8 * 1024 * 1024 || total + source.sizeBytes() > 128L * 1024 * 1024)
                throw new IOException("Migrated artifact exceeds the TerraPack byte budget");
            var result = StructureNbt.read(bytes);
            if(result.dataVersion() != target) throw new IOException("Native migration produced DataVersion " + result.dataVersion() + ", expected " + target);
            if(!original.size().equals(result.size()) || original.blocks().size() != result.blocks().size()
                || original.entities().size() != result.entities().size())
                throw new IOException("Native migration changed template geometry or lost blocks/entities");
            var originalPositions = new HashSet<StructureNbt.Position>();
            var resultPositions = new HashSet<StructureNbt.Position>();
            original.blocks().forEach(block -> originalPositions.add(block.position()));
            result.blocks().forEach(block -> resultPositions.add(block.position()));
            if(!originalPositions.equals(resultPositions)) throw new IOException("Native migration changed block positions");
            var migratedBlocks = new HashMap<StructureNbt.Position, StructureNbt.Block>();
            result.blocks().forEach(block -> migratedBlocks.put(block.position(), block));
            if(original.palettes().size() != result.palettes().size()) throw new IOException("Native migration lost alternative palettes");
            for(var block : original.blocks()) {
                var next = migratedBlocks.get(block.position());
                for(int palette = 0; palette < original.palettes().size(); palette++) {
                    String before = original.palettes().get(palette).get(block.state()).name();
                    String after = result.palettes().get(palette).get(next.state()).name();
                    if(!Set.of("minecraft:air", "minecraft:cave_air", "minecraft:void_air").contains(before)
                        && Set.of("minecraft:air", "minecraft:cave_air", "minecraft:void_air").contains(after))
                        throw new IOException("Native migration replaced a non-air block with air at " + block.position());
                }
                for(String field : List.of("pool", "name", "target", "LootTable"))
                    if(block.nbt().containsKey(field) && !Objects.equals(block.nbt().get(field), next.nbt().get(field)))
                        throw new IOException("Native migration changed " + field + " at " + block.position());
                if(!block.nbt().isEmpty() && next.nbt().isEmpty()) throw new IOException("Native migration lost block entity NBT at " + block.position());
            }
            int jigsaws = jigsaws(original);
            if(jigsaws != jigsaws(result)) throw new IOException("Native migration lost jigsaw connectors");
            migrated.put(path, bytes);
            pieces.add(new Piece(path, original.dataVersion(), target, result.blocks().size(), result.entities().size(), jigsaws));
        } catch(IOException | RuntimeException error) {
            errors.add(path + ": " + error.getMessage());
        }
        // Never publish a partial migrated resource graph: pools may refer to any template.
        if(!errors.isEmpty()) migrated.clear();
        return new Result(new Report(errors.isEmpty() ? "MIGRATED" : "FAILED", target, pieces, errors), migrated);
    }
    private static int jigsaws(StructureNbt.Template template) {
        return (int) template.blocks().stream().filter(block -> template.palettes().getFirst()
            .get(block.state()).name().equals("minecraft:jigsaw")).count();
    }
}
