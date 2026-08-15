package com.dfsek.terra.bukkit.nms.v1_21_8;

import com.dfsek.terra.api.properties.Properties;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public record NMSBiomeInfo(ResourceKey<Biome> biomeKey) implements Properties {
}
