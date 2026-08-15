package com.dfsek.terra.bukkit.generator;

import com.dfsek.terra.api.Handle;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

public class BukkitBiomeProvider extends BiomeProvider implements Handle {
   private final com.dfsek.terra.api.world.biome.generation.BiomeProvider delegate;

   public BukkitBiomeProvider(com.dfsek.terra.api.world.biome.generation.BiomeProvider delegate) {
      this.delegate = delegate;
   }

   @NotNull
   public Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
      com.dfsek.terra.api.world.biome.Biome biome = this.delegate.getBiome(x, y, z, worldInfo.getSeed());
      return (Biome)biome.getPlatformBiome().getHandle();
   }

   @NotNull
   public List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
      return StreamSupport.stream(this.delegate.getBiomes().spliterator(), false)
         .map(terraBiome -> (Biome)terraBiome.getPlatformBiome().getHandle())
         .collect(Collectors.toList());
   }

   @Override
   public Object getHandle() {
      return this.delegate;
   }
}
