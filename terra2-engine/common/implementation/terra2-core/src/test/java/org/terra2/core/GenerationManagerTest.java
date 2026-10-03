package org.terra2.core;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GenerationManagerTest {
    private GenerationPlan<String, String> plan(String id) {
        return new GenerationPlan<>(id, "test", "1", new GenerationProgram<>() {
            public void generate(GenerationContext context, int x, int z, BlockVolume<String> output) {
                output.set(0, context.minY(), 0, id + ":" + context.seed() + ":" + x + ":" + z);
            }
            public String blockAt(GenerationContext context, int x, int y, int z) { return id + ":" + context.seed(); }
            public String biomeAt(GenerationContext context, int x, int y, int z) { return id; }
        });
    }
    private GenerationManager<String, String> manager() { return new GenerationManager<>(Set.of("world")); }
    private GenerationContext context(String name, String dimension, long seed) {
        return new GenerationContext(new WorldTarget(name, dimension), seed, -64, 320, null);
    }
    @Test void defaultRegistryIsEmptyAndGenerationDenied() {
        var manager = manager();
        assertTrue(manager.bindings().isEmpty());
        assertThrows(IllegalStateException.class, () -> manager.blockAt(context("new", "terra2:new", 1), 0, 0, 0));
    }
    @Test void protectsPrimaryWorldAndBuiltinDimensions() {
        var manager = manager();
        assertThrows(IllegalArgumentException.class, () -> manager.authorize(new WorldTarget("world", null), plan("a")));
        for(String dimension : Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"))
            assertThrows(IllegalArgumentException.class, () -> manager.authorize(new WorldTarget("new", dimension), plan("a")));
    }
    @Test void requiresDimensionBindingAndRejectsOtherDimension() {
        var manager = manager(); manager.authorize(new WorldTarget("a", null), plan("a"));
        assertThrows(IllegalStateException.class, () -> manager.blockAt(context("a", null, 1), 0, 0, 0));
        manager.bindDimension("a", "minecraft:a");
        assertEquals("a:1", manager.blockAt(context("a", "minecraft:a", 1), 0, 0, 0));
        assertThrows(IllegalStateException.class, () -> manager.blockAt(context("a", "terra2:other", 1), 0, 0, 0));
    }
    @Test void rejectsActivePlanSeedAndHeightChanges() {
        var manager = manager(); manager.authorize(new WorldTarget("a", "terra2:a"), plan("a"));
        manager.blockAt(context("a", "terra2:a", 1), 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> manager.authorize(new WorldTarget("a", "terra2:a"), plan("b")));
        assertThrows(IllegalStateException.class, () -> manager.blockAt(context("a", "terra2:a", 2), 0, 0, 0));
        assertThrows(IllegalStateException.class, () -> manager.blockAt(new GenerationContext(new WorldTarget("a", "terra2:a"), 1, 0, 256, null), 0, 0, 0));
    }
    @Test void disallowsSharingADimensionBetweenWorlds() {
        var manager = manager(); manager.authorize(new WorldTarget("a", null), plan("a"));
        manager.authorize(new WorldTarget("b", null), plan("b")); manager.bindDimension("a", "terra2:shared");
        assertThrows(IllegalStateException.class, () -> manager.bindDimension("b", "terra2:shared"));
    }
    @Test void independentPlansAndSeedsAreUsedForWorlds() throws Exception {
        var manager = manager();
        manager.authorize(new WorldTarget("a", "terra2:a"), plan("pack-a"));
        manager.authorize(new WorldTarget("b", "terra2:b"), plan("pack-b"));
        try(var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a = executor.submit(() -> manager.blockAt(context("a", "terra2:a", 11), 0, 0, 0));
            var b = executor.submit(() -> manager.blockAt(context("b", "terra2:b", 22), 0, 0, 0));
            assertEquals("pack-a:11", a.get()); assertEquals("pack-b:22", b.get());
        }
        assertEquals("pack-b", manager.biomeAt(context("b", "terra2:b", 22), 1, 2, 3));
    }
    @Test void generationWritesSelectedPlanAndChecksOutputBounds() {
        var manager = manager(); manager.authorize(new WorldTarget("a", "terra2:a"), plan("pack"));
        class Volume implements BlockVolume<String> {
            String state;
            public int minY() { return -64; } public int maxY() { return 320; }
            public String get(int x, int y, int z) { return state; }
            public void set(int x, int y, int z, String state) { this.state = state; }
        }
        var output = new Volume(); manager.generate(context("a", "terra2:a", 7), -2, 3, output);
        assertEquals("pack:7:-2:3", output.state);
        var wrong = new Volume() { public int maxY() { return 256; } };
        assertThrows(IllegalArgumentException.class, () -> manager.generate(context("a", "terra2:a", 7), 0, 0, wrong));
    }
}
