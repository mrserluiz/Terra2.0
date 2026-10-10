package com.dfsek.terra.bukkit.util;

import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class DiscoveryTest {
    @TempDir Path directory;
    @Test void boundedSamplingAndVerticalWorldQueryIsSuppliedByCaller() {
        var found = BiomeSearch.find(10, 20, 64, "FOREST", (x,z) -> x == 42 && z == 20 ? "FOREST" : "OCEAN");
        assertEquals(42, found.x()); assertEquals(20, found.z()); assertTrue(found.samples() > 0);
        assertFalse(found.limited());
        assertTrue(BiomeSearch.find(0,0,64,"missing",(x,z) -> "FOREST").samples() < 0);
        assertTrue(BiomeSearch.find(0,0,8192,"missing",(x,z) -> "FOREST").limited());
        assertThrows(IllegalArgumentException.class, () -> BiomeSearch.find(0,0,8193,"x",(x,z) -> "x"));
    }
    @Test void savesReloadsDeduplicatesAndDoesNotLeakIntoRecreatedWorld() throws Exception {
        var world = UUID.randomUUID(); var other = UUID.randomUUID();
        StructureIndex.configure(directory);
        StructureIndex.record(world,42,"test:castle",10,70,20,"native");
        try(var files = Files.list(directory)) { assertEquals(0, files.count()); } // no generation-thread I/O
        StructureIndex.load(world,42);
        StructureIndex.record(world,42,"test:castle",10,70,20,"native");
        StructureIndex.flush();
        StructureIndex.configure(directory);
        StructureIndex.record(world,42,"test:house",40,80,50,"feature"); // generation during async load
        StructureIndex.load(world,42);
        assertEquals(2, StructureIndex.entries(world).size());
        StructureIndex.load(other,42); assertTrue(StructureIndex.entries(other).isEmpty());
        assertThrows(java.io.IOException.class, () -> StructureIndex.load(world,43));
        StructureIndex.flush();
        assertTrue(StructureIndex.ready(world));
    }
}
