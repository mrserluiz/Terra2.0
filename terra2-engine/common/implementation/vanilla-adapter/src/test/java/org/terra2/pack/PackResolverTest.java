package org.terra2.pack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PackResolverTest {
    @Test void communityDoesNotOpenConvertedArtifact() throws Exception {
        var loads = new AtomicInteger();
        var resolver = new PackResolver<String, String>(id -> Optional.of("legacy"), id -> {}, id -> false,
            id -> { loads.incrementAndGet(); return "converted"; }, () -> "paths");
        assertEquals(PackResolver.Backend.COMMUNITY, resolver.resolve("ANY_PACK").backend());
        assertEquals(0, loads.get());
    }
    @Test void convertedOnlyAndMixedSelectionsResolvePerId() throws Exception {
        var resolver = new PackResolver<String, String>(id -> id.equals("BASE") ? Optional.of("legacy") : Optional.empty(),
            id -> {}, id -> id.equals("OVERLAY"), id -> "converted", () -> "paths");
        assertEquals("legacy", resolver.resolve("BASE").community());
        assertEquals("converted", resolver.resolve("OVERLAY").terraPack());
    }
    @Test void missingIdListsBothPathsWithoutOpeningFile() {
        var resolver = new PackResolver<String, String>(id -> Optional.empty(), id -> {}, id -> false,
            id -> { throw new AssertionError("Must not attempt to load missing .terrapack"); },
            () -> "Community Packs: packs/; TerraPacks: terrapacks/; available: BASE");
        var failure = assertThrows(IllegalArgumentException.class, () -> resolver.resolve("MISSING"));
        assertTrue(failure.getMessage().contains("NOT_FOUND")); assertTrue(failure.getMessage().contains("available: BASE"));
    }
    @Test void collisionIsRefusedBeforeLoadingConvertedPack() {
        var resolver = new PackResolver<String, String>(id -> Optional.of("legacy"), id -> {}, id -> true,
            id -> { throw new AssertionError(); }, () -> "paths");
        assertTrue(assertThrows(IllegalArgumentException.class, () -> resolver.resolve("SAME")).getMessage().contains("collision"));
    }
    @Test void rejectedCommunitySourceRetainsLoaderFailure() {
        var cause = new IllegalStateException("Missing addon");
        var resolver = new PackResolver<String, String>(id -> Optional.empty(),
            id -> { throw new IllegalArgumentException("Community Pack found but rejected", cause); }, id -> false,
            id -> { throw new AssertionError(); }, () -> "paths");
        assertSame(cause, assertThrows(IllegalArgumentException.class, () -> resolver.resolve("ANY_PACK")).getCause());
    }
}
