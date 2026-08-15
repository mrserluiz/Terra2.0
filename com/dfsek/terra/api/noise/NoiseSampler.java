package com.dfsek.terra.api.noise;

import com.dfsek.terra.api.util.vector.Vector2;
import com.dfsek.terra.api.util.vector.Vector2Int;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.util.vector.Vector3Int;

public interface NoiseSampler {
   static NoiseSampler zero() {
      return new NoiseSampler() {
         @Override
         public double noise(long seed, double x, double y) {
            return 0.0;
         }

         @Override
         public double noise(long seed, double x, double y, double z) {
            return 0.0;
         }
      };
   }

   default double noise(Vector3 vector3, long seed) {
      return this.noise(seed, vector3.getX(), vector3.getY(), vector3.getZ());
   }

   default double noise(Vector3Int vector3, long seed) {
      return this.noise(seed, vector3.getX(), vector3.getY(), vector3.getZ());
   }

   default double noise(Vector2 vector2, long seed) {
      return this.noise(seed, vector2.getX(), vector2.getZ());
   }

   default double noise(Vector2Int vector2, long seed) {
      return this.noise(seed, vector2.getX(), vector2.getZ());
   }

   double noise(long var1, double var3, double var5);

   default double noise(long seed, int x, int y) {
      return this.noise(seed, (double)x, (double)y);
   }

   double noise(long var1, double var3, double var5, double var7);

   default double noise(long seed, int x, int y, int z) {
      return this.noise(seed, (double)x, (double)y, (double)z);
   }
}
