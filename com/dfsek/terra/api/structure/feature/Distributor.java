package com.dfsek.terra.api.structure.feature;

public interface Distributor {
   static Distributor yes() {
      return (x, z, seed) -> true;
   }

   static Distributor no() {
      return (x, z, seed) -> false;
   }

   boolean matches(int var1, int var2, long var3);

   default Distributor and(Distributor other) {
      return (x, z, seed) -> this.matches(x, z, seed) && other.matches(x, z, seed);
   }

   default Distributor or(Distributor other) {
      return (x, z, seed) -> this.matches(x, z, seed) || other.matches(x, z, seed);
   }

   default Distributor xor(Distributor other) {
      return (x, z, seed) -> this.matches(x, z, seed) ^ other.matches(x, z, seed);
   }
}
