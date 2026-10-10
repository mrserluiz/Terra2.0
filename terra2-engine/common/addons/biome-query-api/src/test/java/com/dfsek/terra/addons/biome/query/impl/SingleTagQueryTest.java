package com.dfsek.terra.addons.biome.query.impl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import com.dfsek.terra.addons.biome.query.BiomeQueryAPIAddon;
import com.dfsek.terra.api.properties.Context;
import com.dfsek.terra.api.world.biome.Biome;
import static org.junit.jupiter.api.Assertions.*;

class SingleTagQueryTest {
    private Biome biome(List<String> table, Set<String> tags) {
        Context context = new Context();
        Biome biome = (Biome) Proxy.newProxyInstance(Biome.class.getClassLoader(), new Class<?>[] {Biome.class},
            (proxy, method, args) -> switch(method.getName()) {
                case "getTags" -> tags;
                case "getContext" -> context;
                default -> throw new UnsupportedOperationException(method.getName());
            });
        context.put(BiomeQueryAPIAddon.BIOME_TAG_KEY, new BiomeTagHolder(biome, new BiomeTagFlattener(table)));
        return biome;
    }

    @Test void absentTagDoesNotCrashOrMatch() {
        var biome = biome(List.of("CAVERNS_LAND"), Set.of("CAVERNS_LAND"));
        var query = new SingleTagQuery("SPRUCE_CAVERNS");
        assertFalse(query.test(biome));
        assertFalse(query.test(biome));
    }

    @Test void queryUsesEachPacksOwnIndexIncludingMissingTags() {
        var query = new SingleTagQuery("forest");
        var first = biome(List.of("forest", "desert"), Set.of("forest"));
        var second = biome(List.of("desert", "forest"), Set.of("desert"));
        var third = biome(List.of("desert"), Set.of("desert"));
        assertTrue(query.test(first));
        assertFalse(query.test(second));
        assertFalse(query.test(third));
        IntStream.range(0, 10000).parallel().forEach(i -> {
            assertTrue(query.test(first));
            assertFalse(query.test(second));
        });
    }

    @Test void tableIsImmutableAndPreservesFirstDuplicateIndex() {
        var tags = new ArrayList<>(List.of("forest", "forest", "desert"));
        var table = new BiomeTagFlattener(tags);
        tags.clear();
        assertEquals(0, table.index("forest"));
        assertEquals(2, table.index("desert"));
        assertEquals(-1, table.index("missing"));
        assertEquals(3, table.size());
    }
}
