package com.dfsek.terra.api.world.biome;

import com.dfsek.terra.api.properties.PropertyHolder;
import com.dfsek.terra.api.registry.key.StringIdentifiable;
import java.util.Set;

public interface Biome extends PropertyHolder, StringIdentifiable {
   PlatformBiome getPlatformBiome();

   int getColor();

   Set<String> getTags();
}
