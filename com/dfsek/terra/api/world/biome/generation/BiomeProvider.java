package com.dfsek.terra.api.world.biome.generation;

import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.util.Column;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.util.vector.Vector3Int;
import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.api.world.info.WorldProperties;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Contract;

public interface BiomeProvider {
   @Contract(pure = true)
   Biome getBiome(int var1, int var2, int var3, long var4);

   @Contract(pure = true)
   default Biome getBiome(Vector3 vector3, long seed) {
      return this.getBiome(vector3.getBlockX(), vector3.getBlockY(), vector3.getBlockZ(), seed);
   }

   @Contract(pure = true)
   default Biome getBiome(Vector3Int vector3, long seed) {
      return this.getBiome(vector3.getX(), vector3.getY(), vector3.getZ(), seed);
   }

   default Optional<Biome> getBaseBiome(int x, int z, long seed) {
      return Optional.empty();
   }

   default Column<Biome> getColumn(int x, int z, WorldProperties properties) {
      return this.getColumn(x, z, properties.getSeed(), properties.getMinHeight(), properties.getMaxHeight());
   }

   default Column<Biome> getColumn(int x, int z, long seed, int min, int max) {
      return new BiomeColumn(this, min, max, x, z, seed);
   }

   @Contract(pure = true)
   Iterable<Biome> getBiomes();

   @Contract(pure = true)
   default Stream<Biome> stream() {
      return StreamSupport.stream(this.getBiomes().spliterator(), false);
   }

   default CachingBiomeProvider caching(Platform platform) {
      return this instanceof CachingBiomeProvider cachingBiomeProvider ? cachingBiomeProvider : new CachingBiomeProvider(this);
   }

   default int resolution() {
      return 1;
   }
}
