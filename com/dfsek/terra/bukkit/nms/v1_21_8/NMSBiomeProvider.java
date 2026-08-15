package com.dfsek.terra.bukkit.nms.v1_21_8;

import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.bukkit.world.BukkitPlatformBiome;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate.Sampler;
import org.jetbrains.annotations.NotNull;

public class NMSBiomeProvider extends BiomeSource {
   private final BiomeProvider delegate;
   private final long seed;
   private final Registry<Biome> biomeRegistry = RegistryFetcher.biomeRegistry();

   public NMSBiomeProvider(BiomeProvider delegate, long seed) {
      this.delegate = delegate;
      this.seed = seed;
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return this.delegate
         .stream()
         .map(
            biome -> RegistryFetcher.biomeRegistry()
               .getOrThrow(((BukkitPlatformBiome)biome.getPlatformBiome()).getContext().get(NMSBiomeInfo.class).biomeKey())
         );
   }

   @NotNull
   protected MapCodec<? extends BiomeSource> codec() {
      return MapCodec.assumeMapUnsafe(BiomeSource.CODEC);
   }

   @NotNull
   public Holder<Biome> getNoiseBiome(int x, int y, int z, @NotNull Sampler sampler) {
      return this.biomeRegistry
         .getOrThrow(
            ((BukkitPlatformBiome)this.delegate.getBiome(x << 2, y << 2, z << 2, this.seed).getPlatformBiome()).getContext().get(NMSBiomeInfo.class).biomeKey()
         );
   }
}
