package com.dfsek.terra.bukkit.nms;

import com.dfsek.terra.bukkit.world.BukkitBiomeInfo;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttributes;
import java.util.Map;
import java.util.TreeMap;

/** Reads the actual registered biome, including inherited vanilla effects. No chunk/world writes. */
public final class NativeBedrockBiomes {
    private NativeBedrockBiomes() {}
    public static Map<String, JsonObject> export(NMSPlatform platform) {
        Map<String, JsonObject> entries = new TreeMap<>();
        platform.getRawConfigRegistry().forEach(pack -> pack.getRegistry(com.dfsek.terra.api.world.biome.Biome.class).forEach((key, terra) -> {
            var id = ((com.dfsek.terra.bukkit.world.BukkitPlatformBiome) terra.getPlatformBiome()).getContext().get(BukkitBiomeInfo.class).biomeKey().toString();
            var registered = RegistryFetcher.biomeRegistry().get(Identifier.parse(id)).orElseThrow().value();
            JsonObject object = new JsonObject(), effects = new JsonObject(), attributes = new JsonObject();
            effects.addProperty("water_color", color(registered.getWaterColor()));
            var appearance = registered.getSpecialEffects();
            appearance.grassColorOverride().ifPresent(value -> effects.addProperty("grass_color", color(value)));
            appearance.foliageColorOverride().ifPresent(value -> effects.addProperty("foliage_color", color(value)));
            appearance.dryFoliageColorOverride().ifPresent(value -> effects.addProperty("dry_foliage_color", color(value)));
            var environment = registered.getAttributes();
            attributes.addProperty("minecraft:visual/sky_color", color(environment.applyModifier(EnvironmentAttributes.SKY_COLOR, 0x78A7FF)));
            attributes.addProperty("minecraft:visual/fog_color", color(environment.applyModifier(EnvironmentAttributes.FOG_COLOR, 0xC0D8FF)));
            attributes.addProperty("minecraft:visual/water_fog_color", color(environment.applyModifier(EnvironmentAttributes.WATER_FOG_COLOR, 0x050533)));
            object.add("effects", effects); object.add("attributes", attributes);
            if(entries.putIfAbsent(id, object) != null) throw new IllegalStateException("ID de bioma duplicado: " + id);
        }));
        return entries;
    }
    private static String color(int rgb) { return "#%06X".formatted(rgb & 0xFFFFFF); }
}
