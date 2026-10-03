package com.dfsek.terra.bukkit.nms;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import java.util.List;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.bukkit.nms.config.VanillaBiomeProperties;


public class NMSBiomeInjector {

    public static <T> Optional<Holder<T>> getEntry(Registry<T> registry, Identifier identifier) {
        return registry.getOptional(identifier)
            .flatMap(registry::getResourceKey)
            .flatMap(registry::get);
    }

    public static Biome createBiome(Biome vanilla, VanillaBiomeProperties vanillaBiomeProperties)
    throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Biome.BiomeBuilder builder = new Biome.BiomeBuilder().putAttributes(vanilla.getAttributes());

        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder();

        effects.waterColor(Objects.requireNonNullElse(vanillaBiomeProperties.getWaterColor(), vanilla.getWaterColor()))
            .grassColorModifier(Objects.requireNonNullElse(vanillaBiomeProperties.getGrassColorModifier(),
                vanilla.getSpecialEffects().grassColorModifier()));
        // Preserve the vanilla attributes and override only fields explicitly configured by the native pack.
        if(vanillaBiomeProperties.getFogColor() != null) builder.setAttribute(EnvironmentAttributes.FOG_COLOR, vanillaBiomeProperties.getFogColor());
        if(vanillaBiomeProperties.getWaterFogColor() != null) builder.setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, vanillaBiomeProperties.getWaterFogColor());
        if(vanillaBiomeProperties.getSkyColor() != null) builder.setAttribute(EnvironmentAttributes.SKY_COLOR, vanillaBiomeProperties.getSkyColor());
        if(vanillaBiomeProperties.getMusicVolume() != null) builder.setAttribute(EnvironmentAttributes.MUSIC_VOLUME, vanillaBiomeProperties.getMusicVolume());

        if(vanillaBiomeProperties.getGrassColor() == null) {
            vanilla.getSpecialEffects().grassColorOverride().ifPresent(effects::grassColorOverride);
        } else {
            effects.grassColorOverride(vanillaBiomeProperties.getGrassColor());
        }

        if(vanillaBiomeProperties.getFoliageColor() == null) {
            vanilla.getSpecialEffects().foliageColorOverride().ifPresent(effects::foliageColorOverride);
        } else {
            effects.foliageColorOverride(vanillaBiomeProperties.getFoliageColor());
        }

        if(vanillaBiomeProperties.getDryFoliageColor() == null) {
            vanilla.getSpecialEffects().dryFoliageColorOverride().ifPresent(effects::dryFoliageColorOverride);
        } else {
            effects.dryFoliageColorOverride(vanillaBiomeProperties.getDryFoliageColor());
        }
        if(vanillaBiomeProperties.getParticleConfig() != null) {
            builder.setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(vanillaBiomeProperties.getParticleConfig()));
        }
        if(vanillaBiomeProperties.getLoopSound() != null || vanillaBiomeProperties.getMoodSound() != null || vanillaBiomeProperties.getAdditionsSound() != null) {
            AmbientSounds inherited = vanilla.getAttributes().applyModifier(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.EMPTY);
            builder.setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(
                vanillaBiomeProperties.getLoopSound() == null ? inherited.loop() : Optional.of(RegistryFetcher.soundEventRegistry().wrapAsHolder(vanillaBiomeProperties.getLoopSound())),
                vanillaBiomeProperties.getMoodSound() == null ? inherited.mood() : Optional.of(vanillaBiomeProperties.getMoodSound()),
                vanillaBiomeProperties.getAdditionsSound() == null ? inherited.additions() : List.of(vanillaBiomeProperties.getAdditionsSound())));
        }
        if(vanillaBiomeProperties.getMusic() != null) {
            builder.setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(vanillaBiomeProperties.getMusic()));
        }

        builder.hasPrecipitation(Objects.requireNonNullElse(vanillaBiomeProperties.getPrecipitation(), vanilla.hasPrecipitation()));

        builder.temperature(Objects.requireNonNullElse(vanillaBiomeProperties.getTemperature(), vanilla.getBaseTemperature()));

        builder.downfall(Objects.requireNonNullElse(vanillaBiomeProperties.getDownfall(), vanilla.climateSettings.downfall()));

        builder.temperatureAdjustment(
            Objects.requireNonNullElse(vanillaBiomeProperties.getTemperatureModifier(), vanilla.climateSettings.temperatureModifier()));

        builder.mobSpawnSettings(Objects.requireNonNullElse(vanillaBiomeProperties.getSpawnSettings(), vanilla.getMobSettings()));

        return builder
            .specialEffects(effects.build())
            .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
            .build();
    }

    public static String createBiomeID(ConfigPack pack, com.dfsek.terra.api.registry.key.RegistryKey biomeID) {
        return pack.getID()
                   .toLowerCase() + "/" + biomeID.getNamespace().toLowerCase(Locale.ROOT) + "/" + biomeID.getID().toLowerCase(Locale.ROOT);
    }
}
