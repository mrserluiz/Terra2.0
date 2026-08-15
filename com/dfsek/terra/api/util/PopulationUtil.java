package com.dfsek.terra.api.util;

import com.dfsek.terra.api.world.chunk.Chunk;
import java.util.Random;

public final class PopulationUtil {
   public static Random getRandom(Chunk c) {
      return getRandom(c, 0L);
   }

   public static Random getRandom(Chunk c, long salt) {
      return new Random(getCarverChunkSeed(c.getX(), c.getZ(), c.getWorld().getSeed() + salt));
   }

   public static long getCarverChunkSeed(int chunkX, int chunkZ, long seed) {
      Random r = new Random(seed);
      return chunkX * r.nextLong() ^ chunkZ * r.nextLong() ^ seed;
   }
}
