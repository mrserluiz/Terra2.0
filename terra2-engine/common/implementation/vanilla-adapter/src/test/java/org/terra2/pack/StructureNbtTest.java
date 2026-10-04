package org.terra2.pack;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class StructureNbtTest {
    @TempDir Path directory;
    private static void tag(DataOutputStream out, int type, String name) throws IOException { out.writeByte(type); out.writeUTF(name); }
    private static void vector(DataOutputStream out, String name, int x, int y, int z) throws IOException {
        tag(out, 9, name); out.writeByte(3); out.writeInt(3); out.writeInt(x); out.writeInt(y); out.writeInt(z);
    }
    static byte[] fixture(boolean invalidIndex, boolean outside, boolean compressed) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try(var out = new DataOutputStream(bytes)) {
            tag(out, 10, ""); tag(out, 3, "DataVersion"); out.writeInt(4440); vector(out, "size", 2, 1, 1);
            tag(out, 9, "palette"); out.writeByte(10); out.writeInt(2);
            tag(out, 8, "Name"); out.writeUTF("minecraft:jigsaw"); out.writeByte(0);
            tag(out, 8, "Name"); out.writeUTF("minecraft:chest"); out.writeByte(0);
            tag(out, 9, "blocks"); out.writeByte(10); out.writeInt(2);
            vector(out, "pos", outside ? 2 : 0, 0, 0); tag(out, 3, "state"); out.writeInt(invalidIndex ? 2 : 0);
            tag(out, 10, "nbt"); tag(out, 8, "pool"); out.writeUTF("test:rooms"); tag(out, 8, "name"); out.writeUTF("test:door"); out.writeByte(0); out.writeByte(0);
            vector(out, "pos", 1, 0, 0); tag(out, 3, "state"); out.writeInt(1);
            tag(out, 10, "nbt"); tag(out, 8, "LootTable"); out.writeUTF("test:chests/dungeon"); out.writeByte(0); out.writeByte(0);
            tag(out, 9, "entities"); out.writeByte(10); out.writeInt(0); out.writeByte(0);
        }
        if(!compressed) return bytes.toByteArray();
        var zipped = new ByteArrayOutputStream(); try(var gzip = new GZIPOutputStream(zipped)) { gzip.write(bytes.toByteArray()); }
        return zipped.toByteArray();
    }
    @Test void decodesCompressedAndRawTemplatesPreservingJigsawAndLootNbt() throws Exception {
        var raw = StructureNbt.read(fixture(false, false, false));
        assertEquals(raw, StructureNbt.read(fixture(false, false, true)));
        assertEquals(4440, raw.dataVersion()); assertEquals("test:rooms", raw.blocks().getFirst().nbt().get("pool"));
        assertEquals("test:chests/dungeon", raw.blocks().get(1).nbt().get("LootTable"));
        assertEquals("minecraft:chest", raw.palettes().getFirst().get(1).name());
        assertThrows(UnsupportedOperationException.class, () -> raw.blocks().clear());
    }
    @Test void rejectsBrokenStateBoundsTruncationAndExpansionBomb() throws Exception {
        assertThrows(IOException.class, () -> StructureNbt.read(fixture(true, false, true)));
        assertThrows(IOException.class, () -> StructureNbt.read(fixture(false, true, true)));
        assertThrows(IOException.class, () -> StructureNbt.read(new byte[]{10, 0, 0, 9, 0, 1, 65, 3, 127, -1, -1, -1}));
        byte[] raw = fixture(false, false, false);
        assertThrows(IOException.class, () -> StructureNbt.read(Arrays.copyOf(raw, raw.length - 1)));
        var bytes = new ByteArrayOutputStream();
        try(var zip = new GZIPOutputStream(bytes)) { byte[] chunk = new byte[1024 * 1024]; for(int i = 0; i < 17; i++) zip.write(chunk); }
        assertThrows(IOException.class, () -> StructureNbt.read(bytes.toByteArray()));
    }
    @Test void accommodatesLargeLegitimateNbtWithinSeparateNodeAndArrayBudgets() throws Exception {
        byte[] raw = fixture(false, false, false); var bytes = new ByteArrayOutputStream();
        bytes.write(raw, 0, raw.length - 1);
        try(var out = new DataOutputStream(bytes)) {
            tag(out, 7, "extra_source_data"); out.writeInt(1000000); out.write(new byte[1000000]); out.writeByte(0);
        }
        assertEquals(2, StructureNbt.read(bytes.toByteArray()).blocks().size());
    }
    @Test void preservesEmptyRemovalTemplatesAsNoOpPieces() throws Exception {
        var bytes = new ByteArrayOutputStream();
        try(var out = new DataOutputStream(bytes)) {
            tag(out, 10, ""); tag(out, 3, "DataVersion"); out.writeInt(4440); vector(out, "size", 1, 1, 1);
            for(String field : List.of("palette", "blocks", "entities")) {
                tag(out, 9, field); out.writeByte(10); out.writeInt(0);
            }
            out.writeByte(0);
        }
        var piece = StructureNbt.read(bytes.toByteArray());
        assertTrue(piece.blocks().isEmpty()); assertTrue(piece.palettes().getFirst().isEmpty());
    }
    @Test void tracksLocalMissingAndUnvalidatedVanillaDependenciesWithoutMarkingStructuresReady() throws Exception {
        Path input = directory.resolve("conversion/input/source"); Files.createDirectories(input);
        Files.writeString(input.resolve("pack.mcmeta"), "{\"pack\":{\"description\":\"Structure fixture\",\"pack_format\":88}}");
        Path piece = input.resolve("data/test/structure/room.nbt"); Files.createDirectories(piece.getParent()); Files.write(piece, fixture(false, false, true));
        Path pool = input.resolve("data/test/worldgen/template_pool/rooms.json"); Files.createDirectories(pool.getParent());
        Files.writeString(pool, """
            {"fallback":"minecraft:empty","elements":[{"weight":1,"element":{"element_type":"minecraft:single_pool_element",
            "location":"test:room","processors":"minecraft:empty","projection":"rigid"}}]}
            """);
        var store = new TerraPackStore(directory); var conversion = store.convert("Dungeon", List.of("source"), null);
        assertEquals("BLOCKED", conversion.status()); assertThrows(IllegalArgumentException.class, () -> store.load("Dungeon"));
        var report = StructureCatalog.audit(ResourceBundle.read(input));
        assertEquals(1, report.pieces().size()); assertEquals(1, report.pieces().getFirst().jigsaws());
        assertTrue(report.references().stream().anyMatch(ref -> ref.target().equals("test:room") && ref.resolution().equals("LOCAL")));
        assertTrue(report.references().stream().anyMatch(ref -> ref.target().equals("minecraft:empty") && ref.resolution().equals("VANILLA_EXTERNAL_UNVALIDATED")));
        assertTrue(report.errors().stream().anyMatch(error -> error.contains("test:chests/dungeon")));
        assertEquals(1, store.inspect("Dungeon").getAsJsonObject("structureMigration").getAsJsonArray("pieces").size());
    }
}
