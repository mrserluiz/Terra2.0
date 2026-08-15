package com.dfsek.terra.api.util.cache;

public class DoubleSeededVector2Key {
   public double x;
   public double z;
   public long seed;

   public DoubleSeededVector2Key(double x, double z, long seed) {
      this.x = x;
      this.z = z;
      this.seed = seed;
   }

   public void set(double x, double z, long seed) {
      this.x = x;
      this.z = z;
      this.seed = seed;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof DoubleSeededVector2Key that) ? false : this.z == that.z && this.x == that.x && this.seed == that.seed;
   }

   @Override
   public int hashCode() {
      int code = (int)Double.doubleToLongBits(this.x);
      code = 31 * code + (int)Double.doubleToLongBits(this.z);
      return 31 * code + Long.hashCode(this.seed);
   }
}
