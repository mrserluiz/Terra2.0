package br.com.mrserluiz.terra2.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class WorldManifestStoreTest {
    @TempDir Path root;

    @Test void storesManifestInPlatformResolvedDimensionFolder() throws Exception {
        Path dimension = root.resolve("world/dimensions/terra2/resource");
        WorldManifestStore store = new WorldManifestStore(name -> dimension);
        WorldManifest expected = new WorldManifest("resource", "OVERWORLD", "1.0", 100L);
        store.write(expected);
        assertEquals(expected, store.read("resource").orElseThrow());
        assertTrue(java.nio.file.Files.exists(dimension.resolve(".terra2/manifest.properties")));
        assertFalse(java.nio.file.Files.exists(root.resolve("resource")));
    }

    @Test void unresolvedDimensionFailsWithoutCreatingDirectories() {
        WorldManifestStore store = new WorldManifestStore(name -> {
            throw new IllegalStateException("Unresolved dimension");
        });
        assertThrows(IllegalStateException.class, () -> store.read("resource"));
        assertFalse(java.nio.file.Files.exists(root.resolve("resource")));
    }
}
