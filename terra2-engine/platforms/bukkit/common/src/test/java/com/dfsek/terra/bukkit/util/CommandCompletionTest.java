package com.dfsek.terra.bukkit.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandCompletionTest {
    private List<String> complete(boolean permitted, String... args) {
        return CommandCompletion.suggest("terra2", args, permitted, List.of("custom"), List.of("OVERWORLD", "HYDRAXIA"),
            List.of("Debris"), List.of("flat-demo"), List.of("dungeons.zip", "scatter-demo"));
    }
    @Test void permissionsPrefixesAliasesAndDynamicIds() {
        assertEquals(List.of(), complete(false, ""));
        assertEquals(List.of("unlock"), complete(true, "UN"));
        assertEquals(List.of("locate"), complete(true, "loc"));
        assertEquals(List.of("biome"), complete(true, "bio"));
        assertEquals(List.of("Cpack"), complete(true, "lis", "c"));
        assertEquals(List.of("custom"), complete(true, "unlock", "cu"));
        assertEquals(List.of("HYDRAXIA"), complete(true, "unlock", "new", "hyd"));
        assertEquals(List.of("Debris"), complete(true, "packs", "inspect", "D"));
        assertEquals(List.of(), complete(true, "packs", "list", ""));
        assertEquals(List.of("flat-demo"), complete(true, "datapack", "inspect", "f"));
        assertEquals(List.of("dungeons.zip"), complete(true, "convert", "DNT", "du"));
    }
    @Test void compositionCompletesLastIdWithoutSuggestingDuplicates() {
        assertEquals(List.of("OVERWORLD;Debris"), complete(true, "unlock", "custom", "OVERWORLD;d"));
        assertFalse(complete(true, "unlock", "custom", "OVERWORLD;").contains("OVERWORLD;OVERWORLD"));
        assertEquals(List.of("dungeons.zip;scatter-demo"), complete(true, "convert", "Mix", "dungeons.zip;sc"));
    }
}
