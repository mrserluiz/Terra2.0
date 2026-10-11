package com.dfsek.terra.bukkit.compatibility;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.terra2.api.biome.BiomeIdentity;

class BiomeIdentityChunkTest {
    private final UUID world=UUID.randomUUID();
    private BiomeIdentityChunk fixture() {
        int[] cells=new int[384*4];Arrays.fill(cells,0,16,1);
        return new BiomeIdentityChunk(world,42,-1,-2,-64,384,"HYDRAXIA","hash",
            List.of(new BiomeIdentity("SNOW","terra:hydraxia/snow","minecraft:snowy_plains"),
                    new BiomeIdentity("CAVE","terra:hydraxia/cave","minecraft:deep_dark")),cells);
    }
    @Test void roundTripKeepsNegativeCoordinatesAndVerticalBiomes() throws Exception {
        var chunk=BiomeIdentityChunk.decode(fixture().encode());
        assertEquals(world,chunk.world);assertEquals(42,chunk.seed);
        assertEquals("CAVE",chunk.at(-16,-64,-32).orElseThrow().terraId());
        assertEquals("CAVE",chunk.at(-1,-61,-17).orElseThrow().terraId());
        assertEquals("SNOW",chunk.at(-16,-60,-32).orElseThrow().terraId());
        assertTrue(chunk.at(0,64,-32).isEmpty());assertTrue(chunk.at(-16,320,-32).isEmpty());
    }
    @Test void rejectsCorruptionTruncationAndFutureSchemas() throws Exception {
        byte[] encoded=fixture().encode();encoded[30]^=1;
        assertThrows(java.io.IOException.class,()->BiomeIdentityChunk.decode(encoded));
        byte[] valid=fixture().encode();
        assertThrows(java.io.IOException.class,()->BiomeIdentityChunk.decode(Arrays.copyOf(valid,valid.length-4)));
        valid[7]=2;assertThrows(java.io.IOException.class,()->BiomeIdentityChunk.decode(valid));
        assertThrows(java.io.IOException.class,()->BiomeIdentityChunk.decode(null));
    }
    @Test void rejectsInvalidPaletteIndexesBeforeSaving() {
        assertThrows(IllegalArgumentException.class,()->new BiomeIdentityChunk(world,1,0,0,-64,4,"P","H",
            List.of(new BiomeIdentity("A","a:a","minecraft:plains")),new int[1]));
        int[] cells=new int[16];cells[3]=1;
        assertThrows(IllegalArgumentException.class,()->new BiomeIdentityChunk(world,1,0,0,-64,4,"P","H",
            List.of(new BiomeIdentity("A","a:a","minecraft:plains")),cells));
    }
}
