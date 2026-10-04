package org.terra2.adapter.vanilla;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class DatapackManifestTest {
    @TempDir Path folder;
    @Test void restartAcceptsIdenticalSourceAndRefusesChangedSeedPlanDimensionAndHeight() throws Exception {
        Path file = folder.resolve("terra2-generation.json");
        var manifest = new DatapackManifest("dp", "minecraft:dp", 42L, "vanilla-datapack:test:flat@hash", "example#test:flat", "test:flat", -64, 320);
        manifest.verifyOrCreate(file); String original = Files.readString(file);
        manifest.verifyOrCreate(file); assertEquals(original, Files.readString(file));
        for(var changed : new DatapackManifest[]{
            new DatapackManifest("dp", "minecraft:dp", 43L, manifest.plan(), manifest.selection(), "test:flat", -64, 320),
            new DatapackManifest("dp", "minecraft:dp", 42L, "different", manifest.selection(), "test:flat", -64, 320),
            new DatapackManifest("dp", "test:dp", 42L, manifest.plan(), manifest.selection(), "test:flat", -64, 320),
            new DatapackManifest("dp", "minecraft:dp", 42L, manifest.plan(), manifest.selection(), "test:flat", 0, 256)}) {
            assertThrows(IllegalArgumentException.class, () -> changed.verifyOrCreate(file));
            assertEquals(original, Files.readString(file));
        }
        Files.writeString(file, "{}");
        assertThrows(IllegalArgumentException.class, () -> manifest.verifyOrCreate(file));
        assertEquals("{}", Files.readString(file));
    }
}
