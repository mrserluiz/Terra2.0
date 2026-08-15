package com.dfsek.terra.bukkit.config;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.terra.api.properties.Properties;

public class PreLoadCompatibilityOptions implements ConfigTemplate, Properties {
   @Value("minecraft.use-vanilla-biomes")
   @Default
   private boolean vanillaBiomes = false;
   @Value("minecraft.beard.enable")
   @Default
   private boolean beard = true;
   @Value("minecraft.beard.threshold")
   @Default
   private double beardThreshold = 0.5;
   @Value("minecraft.beard.air-threshold")
   @Default
   private double airThreshold = -0.5;

   public boolean useVanillaBiomes() {
      return this.vanillaBiomes;
   }

   public boolean isBeard() {
      return this.beard;
   }

   public double getBeardThreshold() {
      return this.beardThreshold;
   }

   public double getAirThreshold() {
      return this.airThreshold;
   }
}
