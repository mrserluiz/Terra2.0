package com.dfsek.terra.bukkit.compatibility;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class BiomeWorldManifestTest {
    @TempDir Path folder;
    @Test void pinsModeWorldSeedAndPackAcrossRestarts() throws Exception {
        UUID id=UUID.randomUUID();
        BiomeWorldManifest.bind(folder,id,42,true,"hash","test");
        BiomeWorldManifest.bind(folder,id,42,true,"hash","test");
        assertThrows(java.io.IOException.class,()->BiomeWorldManifest.bind(folder,id,43,true,"hash","test"));
        assertThrows(java.io.IOException.class,()->BiomeWorldManifest.bind(folder,id,42,false,"hash","test"));
        assertThrows(java.io.IOException.class,()->BiomeWorldManifest.bind(folder,id,42,true,"new","test"));
    }
    @Test void refusesAnExistingWorldWithoutModifyingItsRegions() throws Exception {
        Path region=folder.resolve("region");Files.createDirectories(region);Path file=region.resolve("r.0.0.mca");
        Files.writeString(file,"unchanged");UUID id=UUID.randomUUID();
        assertThrows(java.io.IOException.class,()->BiomeWorldManifest.bind(folder,id,42,true,"hash","test"));
        assertEquals("unchanged",Files.readString(file));
        assertFalse(Files.exists(folder.resolve("terra2-biomes-"+id+".properties")));
        BiomeWorldManifest.bind(folder,id,42,false,"hash","test");
    }
}
