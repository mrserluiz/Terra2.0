package com.dfsek.terra.bukkit.nms;

import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import com.dfsek.terra.bukkit.nms.config.VanillaBiomeProperties;
import static org.junit.jupiter.api.Assertions.*;

class BiomeMigrationTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test void resolvesModernReflectionBindings() {
        assertNotNull(Reflection.CHUNKMAP);
        assertNotNull(Reflection.MAPPED_REGISTRY);
        assertNotNull(Reflection.VILLAGER_TYPE);
    }
    private Biome original() {
        return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.7f).downfall(0.2f)
            .setAttribute(EnvironmentAttributes.FOG_COLOR, NativeColor.rgb(0x123456))
            .setAttribute(EnvironmentAttributes.SKY_COLOR, NativeColor.rgb(0xabcdef))
            .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x112233)
                .grassColorOverride(0x224466).dryFoliageColorOverride(0x334455).build())
            .mobSpawnSettings(MobSpawnSettings.EMPTY)
            .generationSettings(new BiomeGenerationSettings.PlainBuilder().build()).build();
    }

    @Test void preservesUnspecifiedVanillaAttributesAndClimate() throws Exception {
        Biome original = original();
        Biome migrated = NMSBiomeInjector.createBiome(original, new VanillaBiomeProperties());
        assertEquals(original.getAttributes(), migrated.getAttributes());
        assertEquals(original.getSpecialEffects(), migrated.getSpecialEffects());
        assertFalse(migrated.hasPrecipitation());
        assertEquals(original.getBaseTemperature(), migrated.getBaseTemperature());
        assertEquals(original.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,MobSpawnSettings.EMPTY),
            migrated.getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,MobSpawnSettings.EMPTY));
    }

    @Test void mapsLegacyColorsWithoutDiscardingOtherAttributes() throws Exception {
        VanillaBiomeProperties properties = new VanillaBiomeProperties();
        var fog = VanillaBiomeProperties.class.getDeclaredField("fogColor");
        fog.setAccessible(true);
        fog.set(properties, 0x987654);
        Biome migrated = NMSBiomeInjector.createBiome(original(), properties);
        assertEquals(0x987654, NativeColor.rgb24(migrated.getAttributes().applyModifier(EnvironmentAttributes.FOG_COLOR, NativeColor.rgb(0))));
        assertEquals(0xabcdef, NativeColor.rgb24(migrated.getAttributes().applyModifier(EnvironmentAttributes.SKY_COLOR, NativeColor.rgb(0))));
        assertEquals(0x112233, migrated.getWaterColor());
    }
}
