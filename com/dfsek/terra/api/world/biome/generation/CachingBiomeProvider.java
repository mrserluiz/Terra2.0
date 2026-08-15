package com.dfsek.terra.api.world.biome.generation;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.util.cache.CacheUtils;
import com.dfsek.terra.api.util.cache.SeededVector2Key;
import com.dfsek.terra.api.util.cache.SeededVector3Key;
import com.dfsek.terra.api.util.generic.pair.Pair;
import com.dfsek.terra.api.world.biome.Biome;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import java.util.Optional;

public class CachingBiomeProvider implements BiomeProvider, Handle {
   protected final BiomeProvider delegate;
   private final int res;
   private final ThreadLocal<Pair.Mutable<SeededVector3Key, LoadingCache<SeededVector3Key, Biome>>> cache;
   private final ThreadLocal<Pair.Mutable<SeededVector2Key, LoadingCache<SeededVector2Key, Optional<Biome>>>> baseCache;

   protected CachingBiomeProvider(BiomeProvider delegate) {
      this.delegate = delegate;
      this.res = delegate.resolution();
      this.baseCache = ThreadLocal.withInitial(
         () -> {
            LoadingCache<SeededVector2Key, Optional<Biome>> cache = Caffeine.newBuilder()
               .executor(CacheUtils.CACHE_EXECUTOR)
               .scheduler(Scheduler.systemScheduler())
               .initialCapacity(256)
               .maximumSize(256L)
               .build(this::sampleBiome);
            return Pair.of(new SeededVector2Key(0, 0, 0L), cache).mutable();
         }
      );
      this.cache = ThreadLocal.withInitial(
         () -> {
            LoadingCache<SeededVector3Key, Biome> cache3D = Caffeine.newBuilder()
               .executor(CacheUtils.CACHE_EXECUTOR)
               .scheduler(Scheduler.systemScheduler())
               .initialCapacity(981504)
               .maximumSize(981504L)
               .build(this::sampleBiome);
            return Pair.of(new SeededVector3Key(0, 0, 0, 0L), cache3D).mutable();
         }
      );
   }

   private Optional<Biome> sampleBiome(SeededVector2Key vec) {
      this.baseCache.get().setLeft(new SeededVector2Key(0, 0, 0L));
      return this.delegate.getBaseBiome(vec.x * this.res, vec.z * this.res, vec.seed);
   }

   private Biome sampleBiome(SeededVector3Key vec) {
      this.cache.get().setLeft(new SeededVector3Key(0, 0, 0, 0L));
      return this.delegate.getBiome(vec.x * this.res, vec.y * this.res, vec.z * this.res, vec.seed);
   }

   public BiomeProvider getHandle() {
      return this.delegate;
   }

   @Override
   public Biome getBiome(int x, int y, int z, long seed) {
      Pair.Mutable<SeededVector3Key, LoadingCache<SeededVector3Key, Biome>> cachePair = this.cache.get();
      SeededVector3Key mutableKey = cachePair.getLeft();
      mutableKey.set(x, y, z, seed);
      return cachePair.getRight().get(mutableKey);
   }

   @Override
   public Optional<Biome> getBaseBiome(int x, int z, long seed) {
      Pair.Mutable<SeededVector2Key, LoadingCache<SeededVector2Key, Optional<Biome>>> cachePair = this.baseCache.get();
      SeededVector2Key mutableKey = cachePair.getLeft();
      mutableKey.set(x, z, seed);
      return cachePair.getRight().get(mutableKey);
   }

   @Override
   public Iterable<Biome> getBiomes() {
      return this.delegate.getBiomes();
   }

   @Override
   public int resolution() {
      return this.delegate.resolution();
   }
}
