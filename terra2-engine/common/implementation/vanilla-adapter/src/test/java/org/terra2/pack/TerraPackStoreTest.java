package org.terra2.pack;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.terra2.core.*;
import static org.junit.jupiter.api.Assertions.*;

class TerraPackStoreTest {
    @TempDir Path directory;
    private static final String META = "{\"pack\":{\"pack_format\":48,\"description\":\"Legacy source\"}}";
    private Path source(String name) throws Exception {
        Path path = directory.resolve("conversion/input/" + name); Files.createDirectories(path); Files.writeString(path.resolve("pack.mcmeta"), META); return path;
    }
    private void write(Path root, String path, String data) throws Exception {
        Path file = root.resolve(path); Files.createDirectories(file.getParent()); Files.writeString(file, data);
    }
    private void flat(Path root) throws Exception {
        write(root, "data/test/dimension/flat.json", """
          {"type":"minecraft:overworld","generator":{"type":"minecraft:flat","settings":{
          "biome":"minecraft:plains","layers":[{"height":1,"block":"minecraft:bedrock"},{"height":2,"block":"minecraft:stone"}],
          "features":false,"lakes":false,"structure_overrides":[]}}}
          """);
    }
    private void scatter(Path root) throws Exception {
        write(root, "data/test/worldgen/configured_feature/debris.json", """
            {"type":"minecraft:simple_block","config":{"to_place":{"type":"minecraft:simple_state_provider","state":{"Name":"minecraft:mossy_cobblestone"}}}}
            """);
        write(root, "data/test/worldgen/placed_feature/debris.json", """
            {"feature":"test:debris","placement":[{"type":"minecraft:count","count":3},{"type":"minecraft:in_square"},
            {"type":"minecraft:heightmap","heightmap":"WORLD_SURFACE_WG"}]}
            """);
    }
    @Test void convertsOldFlatSchemaToNeutralPackAndComposesAdditiveFeaturesWithoutReplacingTerrain() throws Exception {
        var store = new TerraPackStore(directory);
        Path base = source("base"), addon = source("addon"); flat(base); scatter(addon);
        assertEquals("READY", store.convert("Base", List.of("base"), "test:flat").status());
        assertEquals("READY", store.convert("Debris", List.of("addon"), null).status());
        TerraPack terrain = store.load("Base"), extension = store.load("Debris");
        TerraPackStore.validateComposition(List.of(terrain, extension), false);
        TerraPackStore.validateComposition(List.of(extension), true);
        var flat = terrain.terrain().compile(block -> block, biome -> biome);
        var plan = PlanComposition.compose("Base;Debris", terrain.fingerprint() + extension.fingerprint(), flat,
            TerraPackStore.features(List.of(terrain, extension)), block -> block, block -> block.equals("minecraft:air"));
        var manager = new GenerationManager<String, String>(Set.of("world"));
        manager.authorize(new WorldTarget("isolated", null), plan); manager.bindDimension("isolated", "test:world");
        var context = new GenerationContext(manager.binding("isolated").target(), 42, -64, 320, null);
        var blocks = new HashMap<String, String>();
        BlockVolume<String> volume = new BlockVolume<>() {
            public int minY() { return -64; } public int maxY() { return 320; }
            public String get(int x, int y, int z) { return blocks.getOrDefault(x + "/" + y + "/" + z, "minecraft:air"); }
            public void set(int x, int y, int z, String value) { blocks.put(x + "/" + y + "/" + z, value); }
        };
        manager.generate(context, -1, 2, volume);
        assertEquals(768, blocks.size()); manager.decorate(context, -1, 2, volume);
        assertTrue(blocks.values().contains("minecraft:mossy_cobblestone"));
        assertEquals("minecraft:stone", manager.blockAt(context, 0, -62, 0));
        assertEquals("minecraft:bedrock", volume.get(0, -64, 0));
        assertThrows(IllegalArgumentException.class, () -> TerraPackStore.validateComposition(List.of(terrain), true));
        assertThrows(IllegalArgumentException.class, () -> TerraPackStore.validateComposition(List.of(terrain, terrain), false));
        assertThrows(IllegalArgumentException.class, () -> TerraPackStore.validateComposition(List.of(terrain, extension, extension), false));
        var merged = store.convert("Merged", List.of("base", "addon"), "test:flat");
        assertEquals("READY", merged.status()); assertEquals(1, store.load("Merged").features().size());
        assertEquals(2, store.inspect("Merged").getAsJsonArray("sources").size());
        assertThrows(java.io.IOException.class, () -> store.convert("Base", List.of("base"), "test:flat"));
        assertTrue(Files.exists(base.resolve("pack.mcmeta")));
    }
    @Test void emitsBlockedDraftAndReportForStructuresFunctionsAndSecondarySourceOverlay() throws Exception {
        var store = new TerraPackStore(directory); Path source = source("complex");
        write(source, "data/test/worldgen/structure/dungeon.json", "{\"type\":\"minecraft:jigsaw\"}");
        write(source, "data/test/function/load.mcfunction", "say never execute me");
        var result = store.convert("DungeonDraft", List.of("complex"), null);
        assertEquals("BLOCKED", result.status()); assertTrue(Files.exists(result.report()));
        assertThrows(IllegalArgumentException.class, () -> store.load("DungeonDraft"));
        assertTrue(store.inspect("DungeonDraft").getAsJsonObject("compiled").getAsJsonArray("blockers").size() >= 2);
        Path base = source("base"), overlays = source("overlays"); flat(base);
        Files.writeString(overlays.resolve("pack.mcmeta"), "{\"pack\":{\"description\":\"test\"},\"overlays\":{\"entries\":[{\"directory\":\"changes\"}]}}");
        assertEquals("BLOCKED", store.convert("OverlayDraft", List.of("base", "overlays"), "test:flat").status());
        assertFalse(Files.exists(directory.resolve("world")));
    }
    @Test void conflictsAndTraversalFailBeforeAnOutputIsPublished() throws Exception {
        var store = new TerraPackStore(directory); Path a = source("a"), b = source("b"); flat(a); flat(b);
        Path file = b.resolve("data/test/dimension/flat.json"); Files.writeString(file, Files.readString(file).replace("minecraft:stone", "minecraft:dirt"));
        assertThrows(IllegalArgumentException.class, () -> store.convert("Conflict", List.of("a", "b"), "test:flat"));
        assertFalse(Files.exists(directory.resolve("terrapacks/Conflict.terrapack")));
        assertThrows(IllegalArgumentException.class, () -> store.convert("../outside", List.of("a"), null));
        assertThrows(IllegalArgumentException.class, () -> store.convert("Safe", List.of("../a"), null));
        assertThrows(IllegalArgumentException.class, () -> store.convert("Safe", List.of("a", "a"), null));
    }
    @Test void normalizesSingleEnclosingFolderButRefusesCollectionsWithoutPublishingOutput() throws Exception {
        var store = new TerraPackStore(directory);
        Path enclosing = directory.resolve("conversion/input/enclosing");
        Path pack = enclosing.resolve("downloaded-pack"); Files.createDirectories(pack); Files.writeString(pack.resolve("pack.mcmeta"), META); flat(pack);
        assertEquals("READY", store.convert("Wrapped", List.of("enclosing"), "test:flat").status());
        assertNotEquals(store.inspect("Wrapped").getAsJsonArray("sources").get(0).getAsJsonObject().get("sha256"),
            store.inspect("Wrapped").getAsJsonArray("sources").get(0).getAsJsonObject().get("archiveSha256"));
        Path second = enclosing.resolve("another"); Files.createDirectories(second); Files.writeString(second.resolve("pack.mcmeta"), META);
        assertThrows(java.io.IOException.class, () -> store.convert("Collection", List.of("enclosing"), null));
        assertFalse(Files.exists(directory.resolve("terrapacks/Collection.terrapack")));
        assertFalse(Files.exists(directory.resolve("conversion/reports/Collection.json")));
    }
}
