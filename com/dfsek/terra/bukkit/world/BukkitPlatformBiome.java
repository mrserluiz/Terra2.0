package com.dfsek.terra.bukkit.world;

import com.dfsek.terra.api.properties.Context;
import com.dfsek.terra.api.properties.PropertyHolder;
import com.dfsek.terra.api.world.biome.PlatformBiome;
import org.bukkit.block.Biome;

public class BukkitPlatformBiome implements PlatformBiome, PropertyHolder {
   private final Biome biome;
   private final Context context = new Context();

   public BukkitPlatformBiome(Biome biome) {
      this.biome = biome;
   }

   public Biome getHandle() {
      return this.biome;
   }

   @Override
   public Context getContext() {
      return this.context;
   }
}
