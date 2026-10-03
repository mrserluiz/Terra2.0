package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;
import org.bukkit.entity.EnderCrystal;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class LegacyEntityTest {
    @Test void retainsCrystalBottomFlag() {
        for(String value : new String[] { "0", "1b" }) {
            var type = (BukkitEntityType) BukkitUtils.getEntityType("minecraft:end_crystal{ShowBottom:" + value + "}");
            assertEquals(org.bukkit.entity.EntityType.END_CRYSTAL, type.getHandle());
            var bottom = new AtomicReference<Boolean>();
            var crystal = (EnderCrystal) Proxy.newProxyInstance(EnderCrystal.class.getClassLoader(),
                new Class<?>[] { EnderCrystal.class }, (proxy, method, args) -> {
                    if(method.getName().equals("setShowingBottom")) bottom.set((Boolean) args[0]);
                    return null;
                });
            assertSame(crystal, type.configure(crystal));
            assertEquals(value.equals("1b"), bottom.get());
        }
    }
    @Test void rejectsUnsupportedDataInsteadOfDroppingIt() {
        assertThrows(IllegalArgumentException.class, () -> BukkitUtils.getEntityType("minecraft:end_crystal{Unknown:0}"));
        assertThrows(IllegalArgumentException.class, () -> BukkitUtils.getEntityType("minecraft:pig{ShowBottom:0}"));
        assertThrows(IllegalArgumentException.class, () -> BukkitUtils.getEntityType("minecraft:end_crystal{ShowBottom:0}junk"));
    }
}
