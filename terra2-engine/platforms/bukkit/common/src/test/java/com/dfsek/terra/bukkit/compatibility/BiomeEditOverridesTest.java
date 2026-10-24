package com.dfsek.terra.bukkit.compatibility;

import org.junit.jupiter.api.Test;
import org.terra2.api.biome.BiomeIdentity;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BiomeEditOverridesTest {
    @Test void sparseEditsSurviveSerializationAndPreserveOtherCells() throws Exception {
        UUID world=UUID.randomUUID();var pink=new BiomeIdentity("PINK","terra:lab/pink","minecraft:plains");
        var blue=new BiomeIdentity("BLUE","terra:lab/blue","minecraft:forest");
        byte[] first=BiomeEditOverrides.edit(null,world,42,-1,0,-64,384,"LAB","fingerprint",Map.of(0,pink)).encode();
        var second=BiomeEditOverrides.edit(first,world,42,-1,0,-64,384,"LAB","fingerprint",Map.of(1,blue));
        var decoded=BiomeIdentityChunk.decode(second.encode());
        assertEquals(pink,decoded.at(-16,-64,0).orElseThrow());assertEquals(blue,decoded.at(-12,-64,0).orElseThrow());
        assertEquals("__UNSET__",decoded.at(-8,-64,0).orElseThrow().terraId());
        assertThrows(java.io.IOException.class,()->BiomeEditOverrides.edit(first,UUID.randomUUID(),42,-1,0,-64,384,"LAB","fingerprint",Map.of(1,blue)));
        assertThrows(java.io.IOException.class,()->BiomeEditOverrides.edit(first,world,42,-1,0,-64,384,"LAB","changed-pack",Map.of(1,blue)));
        first[first.length-1]^=1;assertThrows(java.io.IOException.class,()->BiomeEditOverrides.edit(first,world,42,-1,0,-64,384,"LAB","fingerprint",Map.of(1,blue)));
    }
    @Test void packUpgradeKeepsOnlyExplicitKnownIdentitiesAndNeverAcceptsForeignData() throws Exception {
        UUID world=UUID.randomUUID();var pink=new BiomeIdentity("PINK","terra:lab/pink","minecraft:plains");
        var blue=new BiomeIdentity("BLUE","terra:lab/blue","minecraft:forest");
        byte[] original=BiomeEditOverrides.edit(null,world,42,-1,0,-64,384,"LAB","v1",Map.of(0,pink)).encode();
        var upgraded=BiomeEditOverrides.edit(original,world,42,-1,0,-64,384,"LAB","v2",Map.of(1,blue),List.of(pink,blue));
        assertEquals(pink,upgraded.at(-16,-64,0).orElseThrow());
        assertEquals(blue,upgraded.at(-12,-64,0).orElseThrow());
        assertTrue(BiomeEditOverrides.isUnset(upgraded.at(-8,-64,0).orElseThrow()));
        assertEquals("v2",upgraded.fingerprint);
        assertThrows(java.io.IOException.class,()->BiomeEditOverrides.edit(original,world,42,-1,0,-64,384,"LAB","v2",Map.of(1,blue),List.of(blue)));
        assertThrows(java.io.IOException.class,()->BiomeEditOverrides.edit(original,world,42,-1,0,-64,384,"OTHER","v1",Map.of(1,blue),List.of(pink,blue)));
        assertThrows(java.io.IOException.class,()->BiomeEditOverrides.edit(original,world,99,-1,0,-64,384,"LAB","v2",Map.of(1,blue),List.of(pink,blue)));
        var changedReference=new BiomeIdentity("PINK","terra:lab/pink","minecraft:desert");
        assertThrows(java.io.IOException.class,()->BiomeEditOverrides.edit(original,world,42,-1,0,-64,384,"LAB","v2",Map.of(1,blue),List.of(changedReference,blue)));
    }
}
