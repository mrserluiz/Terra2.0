package com.dfsek.terra.config.pack;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;

public class ConfigPackPostTemplate implements ConfigTemplate {
   @Value("biomes")
   private @Meta BiomeProvider providerBuilder;

   public BiomeProvider getProviderBuilder() {
      return this.providerBuilder;
   }
}
