package com.dfsek.terra.bukkit.nms;
import net.minecraft.world.level.LevelHeightAccessor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WorldHeightBoundsTest {
    @Test void convertsInclusiveNmsHeightToExclusiveTerraBound() {
        LevelHeightAccessor height = new LevelHeightAccessor() {
            public int getMinY() { return -64; }
            public int getHeight() { return 384; }
        };
        assertEquals(319, height.getMaxY());
        var properties = new NMSWorldProperties(42, height);
        assertEquals(-64, properties.getMinHeight());
        assertEquals(320, properties.getMaxHeight());
        assertEquals(height.getHeight(), properties.getMaxHeight() - properties.getMinHeight());
        assertEquals(42, properties.getSeed());
    }
    @Test void handlesDifferentDimensionHeights() {
        LevelHeightAccessor height = new LevelHeightAccessor() {
            public int getMinY() { return 0; }
            public int getHeight() { return 256; }
        };
        assertEquals(256, new NMSWorldProperties(7, height).getMaxHeight());
    }
}
