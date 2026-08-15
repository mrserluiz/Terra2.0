package com.dfsek.terra.api.world.biome.generation;

import com.dfsek.terra.api.util.Column;
import com.dfsek.terra.api.world.biome.Biome;

class BiomeColumn implements Column<Biome> {
   private final BiomeProvider biomeProvider;
   private final int min;
   private final int max;
   private final int x;
   private final int z;
   private final long seed;

   protected BiomeColumn(BiomeProvider biomeProvider, int min, int max, int x, int z, long seed) {
      this.biomeProvider = biomeProvider;
      this.min = min;
      this.max = max;
      this.x = x;
      this.z = z;
      this.seed = seed;
   }

   @Override
   public int getMinY() {
      return this.min;
   }

   @Override
   public int getMaxY() {
      return this.max;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public int getZ() {
      return this.z;
   }

   public Biome get(int y) {
      return this.biomeProvider.getBiome(this.x, y, this.z, this.seed);
   }
}
