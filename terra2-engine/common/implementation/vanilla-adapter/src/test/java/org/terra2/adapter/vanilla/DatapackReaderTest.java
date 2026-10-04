package org.terra2.adapter.vanilla;

import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.terra2.core.*;
import static org.junit.jupiter.api.Assertions.*;

class DatapackReaderTest {
    @TempDir Path folder;
    private static final String META = "{\"pack\":{\"description\":\"test\",\"min_format\":[107,1],\"max_format\":[107,1]}}";
    private static final String FLAT = """
        {"type":"minecraft:overworld","generator":{"type":"minecraft:flat","settings":{
          "biome":"minecraft:plains","layers":[{"height":1,"block":"minecraft:bedrock"},
          {"height":3,"block":"minecraft:dirt"},{"height":1,"block":"minecraft:grass_block"}],
          "features":false,"lakes":false,"structure_overrides":[]}}}
        """;
    private Path pack(String meta, String dimension) throws Exception {
        Path root = folder.resolve(UUID.randomUUID().toString());
        Files.createDirectories(root.resolve("data/test/dimension"));
        Files.writeString(root.resolve("pack.mcmeta"), meta);
        Files.writeString(root.resolve("data/test/dimension/flat.json"), dimension);
        return root;
    }
    @Test void zipAndFolderCompileToSameImmutablePlanAndGenerateOnlyAuthorizedWorld() throws Exception {
        Path root = pack(META, FLAT), zip = folder.resolve("test.zip");
        try(var output = new ZipOutputStream(Files.newOutputStream(zip)); var paths = Files.walk(root)) {
            for(Path path : paths.filter(Files::isRegularFile).toList()) {
                output.putNextEntry(new ZipEntry(root.relativize(path).toString())); output.write(Files.readAllBytes(path)); output.closeEntry();
            }
        }
        var reader = DatapackReader.open(root);
        assertEquals(reader.inspect().fingerprint(), DatapackReader.open(zip).inspect().fingerprint());
        var definition = reader.flat("test:flat");
        var plan = definition.compile(block -> block, biome -> biome);
        var manager = new GenerationManager<String, String>(Set.of("world"));
        var context = new GenerationContext(new WorldTarget("test_world", "test:isolated"), 123L, -64, 320, null);
        assertThrows(IllegalStateException.class, () -> manager.blockAt(context, 0, -64, 0));
        assertThrows(IllegalArgumentException.class, () -> manager.authorize(new WorldTarget("world", null), plan));
        manager.authorize(new WorldTarget("test_world", null), plan); manager.bindDimension("test_world", "test:isolated");
        var volume = new HashMap<String, String>();
        manager.generate(context, -2, 3, new BlockVolume<>() {
            public int minY() { return -64; } public int maxY() { return 320; }
            public void set(int x, int y, int z, String state) { volume.put(x + "/" + y + "/" + z, state); }
            public String get(int x, int y, int z) { return volume.get(x + "/" + y + "/" + z); }
        });
        assertEquals(5 * 256, volume.size());
        assertEquals("minecraft:bedrock", volume.get("0/-64/0"));
        assertEquals("minecraft:dirt", volume.get("15/-61/15"));
        assertEquals("minecraft:grass_block", manager.blockAt(context, 16, -60, 32));
        assertEquals("minecraft:air", manager.blockAt(context, 16, -59, 32));
        assertEquals("minecraft:plains", manager.biomeAt(context, -100, 10, 300));
        Files.writeString(root.resolve("data/test/dimension/flat.json"), FLAT.replace("\"height\":3", "\"height\":4"));
        assertNotEquals(definition.fingerprint(), DatapackReader.open(root).flat("test:flat").fingerprint());
        assertEquals("minecraft:grass_block", plan.program().blockAt(context, 0, -60, 0));
    }
    @Test void refusesUnsupportedGeneratorsFeaturesFieldsAndInvalidHeights() throws Exception {
        for(String invalid : List.of(FLAT.replace("minecraft:flat", "minecraft:noise"),
            FLAT.replace("\"features\":false", "\"features\":true"),
            FLAT.replace("\"lakes\":false", "\"lakes\":\"false\""),
            FLAT.replace("\"structure_overrides\":[]", "\"structure_overrides\":[\"minecraft:villages\"]"),
            FLAT.replace("\"height\":3", "\"height\":3.5"), FLAT.replace("\"height\":3", "\"height\":-1"),
            FLAT.replace("\"height\":3", "\"height\":384"), FLAT.replace("\"biome\":", "\"unknown\":true,\"biome\":"),
            FLAT.replace("minecraft:overworld", "minecraft:the_nether"), FLAT.replace("minecraft:plains", "test:custom"))) {
            var reader = DatapackReader.open(pack(META, invalid));
            assertThrows(IllegalArgumentException.class, () -> reader.flat("test:flat"), invalid);
        }
    }
    @Test void refusesOldFormatsMissingDimensionAndNonWorldgenResources() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> DatapackReader.open(pack("{\"pack\":{\"pack_format\":48}}", FLAT)));
        Path root = pack(META, FLAT);
        assertThrows(IllegalArgumentException.class, () -> DatapackReader.open(root).flat("test:missing"));
        Files.createDirectories(root.resolve("data/test/function")); Files.writeString(root.resolve("data/test/function/load.mcfunction"), "say test");
        var reader = DatapackReader.open(root);
        assertTrue(reader.inspect().unsupported().contains("data/test/function/load.mcfunction"));
        assertThrows(IllegalArgumentException.class, () -> reader.flat("test:flat"));
        assertFalse(Files.exists(folder.resolve("world")));
    }
    @Test void rejectsTraversalSymlinksAndOversizedResourcesWithoutExtraction() throws Exception {
        Path zip = folder.resolve("bad.zip");
        try(var output = new ZipOutputStream(Files.newOutputStream(zip))) {
            output.putNextEntry(new ZipEntry("../outside.txt")); output.write(1); output.closeEntry();
        }
        assertThrows(IllegalArgumentException.class, () -> DatapackReader.open(zip));
        assertFalse(Files.exists(folder.getParent().resolve("outside.txt")));
        Path root = pack(META, FLAT);
        Files.createSymbolicLink(root.resolve("link"), root.resolve("pack.mcmeta"));
        assertThrows(java.io.IOException.class, () -> DatapackReader.open(root));
        Files.delete(root.resolve("link")); Files.write(root.resolve("large.json"), new byte[1024 * 1024 + 1]);
        assertThrows(java.io.IOException.class, () -> DatapackReader.open(root));
    }
}
