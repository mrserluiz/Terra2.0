package com.dfsek.terra.bukkit.world.block.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LegacyBlockSpecTest {
    @Test void keepsBlockPropertiesAndLootSeparate() {
        var spec = LegacyBlockSpec.parse("minecraft:chest[facing=east]{LootTable:'chests/end_city_treasure'}");
        assertEquals("minecraft:chest[facing=east]", spec.blockData());
        assertEquals("minecraft:chests/end_city_treasure", spec.lootTable());
    }
    @Test void normalizesLegacyArchaeologySpelling() {
        var spec = LegacyBlockSpec.parse("minecraft:suspicious_gravel{LootTable:'archaeology/trial_ruins_rare'}");
        assertEquals("minecraft:archaeology/trail_ruins_rare", spec.lootTable());
    }
    @Test void acceptsNamespacedDoubleQuotedTable() {
        assertEquals("custom:chests/test", LegacyBlockSpec.parse("chest{LootTable: \"custom:chests/test\"}").lootTable());
    }
    @Test void neverDiscardsUnsupportedBlockEntityData() {
        assertThrows(IllegalArgumentException.class, () -> LegacyBlockSpec.parse("chest{Items:[]}"));
        assertThrows(IllegalArgumentException.class, () -> LegacyBlockSpec.parse("chest{LootTable:'chests/test'}junk"));
        assertNull(LegacyBlockSpec.parse("stone").lootTable());
    }
}
