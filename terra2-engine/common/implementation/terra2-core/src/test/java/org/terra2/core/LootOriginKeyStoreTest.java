package org.terra2.core;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class LootOriginKeyStoreTest {
    @TempDir Path directory;
    @Test void signedOriginsSurviveRestartAndADamagedKeyIsNotReplaced() throws Exception {
        Path key = directory.resolve("loot-origin.key");
        var first = LootOriginKeyStore.load(key);
        var receipt = first.issue(new WorldTarget("heat", "terra2:heat"), "Dungeon", "test:chest");
        byte[] saved = Files.readAllBytes(key);
        assertTrue(LootOriginKeyStore.load(key).authentic(receipt));
        assertArrayEquals(saved, Files.readAllBytes(key));
        Files.write(key, new byte[]{1});
        assertThrows(java.io.IOException.class, () -> LootOriginKeyStore.load(key));
        assertArrayEquals(new byte[]{1}, Files.readAllBytes(key));
    }
}
