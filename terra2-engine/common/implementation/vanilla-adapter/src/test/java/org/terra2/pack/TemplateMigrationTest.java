package org.terra2.pack;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class TemplateMigrationTest {
    @TempDir Path directory;
    private Path source() throws IOException {
        Path path = directory.resolve("conversion/input/source");
        Files.createDirectories(path.resolve("data/test/structure"));
        Files.writeString(path.resolve("pack.mcmeta"), "{\"pack\":{\"description\":\"Fixture\",\"pack_format\":88}}");
        Files.write(path.resolve("data/test/structure/room.nbt"), StructureNbtTest.fixture(false, false, false));
        return path;
    }
    private static byte[] version(byte[] bytes, int value) {
        var result = bytes.clone();
        // Fixture starts with a root compound followed by its DataVersion int tag.
        ByteBuffer.wrap(result).putInt(17, value);
        return result;
    }
    private static TemplateMigration.Backend backend(int target, java.util.function.Function<byte[], byte[]> action) {
        return new TemplateMigration.Backend() {
            public int targetDataVersion() { return target; }
            public byte[] migrate(byte[] input) { return action.apply(input); }
        };
    }
    private static byte[] replaceUtf(byte[] source, String before, String after) {
        try {
            var pattern = new ByteArrayOutputStream(); new DataOutputStream(pattern).writeUTF(before);
            var replacement = new ByteArrayOutputStream(); new DataOutputStream(replacement).writeUTF(after);
            byte[] needle = pattern.toByteArray();
            for(int i = 0; i <= source.length - needle.length; i++) {
                if(!Arrays.equals(Arrays.copyOfRange(source, i, i + needle.length), needle)) continue;
                var output = new ByteArrayOutputStream(); output.write(source, 0, i); output.write(replacement.toByteArray());
                output.write(source, i + needle.length, source.length - i - needle.length); return output.toByteArray();
            }
            throw new IllegalArgumentException("Fixture string not found");
        } catch(IOException error) { throw new UncheckedIOException(error); }
    }
    @Test void publishesSeparateMigratedTemplatesWithoutMakingStructuresReady() throws Exception {
        Path source = source(); byte[] original = Files.readAllBytes(source.resolve("data/test/structure/room.nbt"));
        var store = new TerraPackStore(directory);
        var conversion = store.convert("Dungeon", List.of("source"), null, backend(5000, input -> version(input, 5000)));
        assertEquals("BLOCKED", conversion.status());
        assertEquals("MIGRATED", store.inspect("Dungeon").getAsJsonObject("nativeTemplateMigration").get("status").getAsString());
        assertThrows(IllegalArgumentException.class, () -> store.load("Dungeon"));
        try(var zip = new ZipFile(conversion.output().toFile())) {
            assertArrayEquals(original, zip.getInputStream(zip.getEntry("resources/data/test/structure/room.nbt")).readAllBytes());
            var migrated = zip.getInputStream(zip.getEntry("native-templates/data/test/structure/room.nbt")).readAllBytes();
            assertEquals(5000, StructureNbt.read(migrated).dataVersion());
            assertEquals("test:rooms", StructureNbt.read(migrated).blocks().getFirst().nbt().get("pool"));
        }
    }
    @Test void rejectsFutureVersionsAndPartialGraphs() throws Exception {
        Path source = source();
        Files.write(source.resolve("data/test/structure/future.nbt"), version(StructureNbtTest.fixture(false, false, false), 6000));
        var migration = TemplateMigration.run(ResourceBundle.read(source), backend(5000, input -> version(input, 5000)));
        assertEquals("FAILED", migration.report().status()); assertTrue(migration.templates().isEmpty());
        assertEquals(1, migration.report().pieces().size());
        assertTrue(migration.report().errors().getFirst().contains("6000"));
    }
    @Test void requiresActualTargetVersionAndValidGeometry() throws Exception {
        var source = ResourceBundle.read(source());
        var unchanged = TemplateMigration.run(source, backend(5000, input -> input));
        assertEquals("FAILED", unchanged.report().status()); assertTrue(unchanged.templates().isEmpty());
        var malformed = TemplateMigration.run(source, backend(5000, input -> new byte[]{0}));
        assertEquals("FAILED", malformed.report().status()); assertTrue(malformed.templates().isEmpty());
        assertEquals("PENDING", TemplateMigration.run(source, null).report().status());
    }
    @Test void rejectsAirFallbackAndLossOfPoolOrLootReferences() throws Exception {
        var source = ResourceBundle.read(source());
        for(var change : Map.of("minecraft:chest", "minecraft:air", "test:rooms", "test:other", "test:chests/dungeon", "test:chests/other").entrySet()) {
            var result = TemplateMigration.run(source, backend(5000, input -> replaceUtf(version(input, 5000), change.getKey(), change.getValue())));
            assertEquals("FAILED", result.report().status(), change.getKey()); assertTrue(result.templates().isEmpty());
        }
    }
    @Test void gameplayExclusionsAreExplicitAndDoNotExcludeLootDependencies() throws Exception {
        Path path = source();
        for(String resource : List.of("function/start.mcfunction", "advancement/start.json", "recipe/start.json",
                                     "loot_table/chest.json", "predicate/check.json", "item_modifier/item.json", "trial_spawner/spawner.json")) {
            Path file = path.resolve("data/test/" + resource); Files.createDirectories(file.getParent());
            Files.writeString(file, resource.endsWith("mcfunction") ? "say fixture" : "{}");
        }
        var bundle = ResourceBundle.read(path);
        var full = PackCompiler.compile(bundle, null);
        var scoped = PackCompiler.compile(bundle, null, PackCompiler.Profile.GENERATION_AND_LOOT);
        assertFalse(scoped.ready());
        assertTrue(full.blockers().stream().anyMatch(value -> value.contains("function/start.mcfunction")));
        assertFalse(scoped.blockers().stream().anyMatch(value -> value.contains("function/start.mcfunction")));
        assertTrue(scoped.notes().stream().anyMatch(value -> value.contains("function/start.mcfunction")));
        for(String kind : List.of("loot_table", "predicate", "item_modifier", "trial_spawner"))
            assertTrue(scoped.blockers().stream().anyMatch(value -> value.contains("/" + kind + "/")));
    }
}
