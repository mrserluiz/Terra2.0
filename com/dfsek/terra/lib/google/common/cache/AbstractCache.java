package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@GwtCompatible
public abstract class AbstractCache<K, V> implements Cache<K, V> {
   protected AbstractCache() {
   }

   @Override
   public V get(K key, Callable<? extends V> valueLoader) throws ExecutionException {
      throw new UnsupportedOperationException();
   }

   @Override
   public ImmutableMap<K, V> getAllPresent(Iterable<? extends Object> keys) {
      Map<K, V> result = Maps.newLinkedHashMap();

      for (Object key : keys) {
         if (!result.containsKey(key)) {
            K castKey = (K)key;
            V value = this.getIfPresent(key);
            if (value != null) {
               result.put(castKey, value);
            }
         }
      }

      return ImmutableMap.copyOf(result);
   }

   @Override
   public void put(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
         this.put((K)entry.getKey(), (V)entry.getValue());
      }
   }

   @Override
   public void cleanUp() {
   }

   @Override
   public long size() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void invalidate(Object key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void invalidateAll(Iterable<? extends Object> keys) {
      for (Object key : keys) {
         this.invalidate(key);
      }
   }

   @Override
   public void invalidateAll() {
      throw new UnsupportedOperationException();
   }

   @Override
   public CacheStats stats() {
      throw new UnsupportedOperationException();
   }

   @Override
   public ConcurrentMap<K, V> asMap() {
      throw new UnsupportedOperationException();
   }

   public static final class SimpleStatsCounter implements AbstractCache.StatsCounter {
      private final LongAddable hitCount = LongAddables.create();
      private final LongAddable missCount = LongAddables.create();
      private final LongAddable loadSuccessCount = LongAddables.create();
      private final LongAddable loadExceptionCount = LongAddables.create();
      private final LongAddable totalLoadTime = LongAddables.create();
      private final LongAddable evictionCount = LongAddables.create();

      @Override
      public void recordHits(int count) {
         this.hitCount.add(count);
      }

      @Override
      public void recordMisses(int count) {
         this.missCount.add(count);
      }

      @Override
      public void recordLoadSuccess(long loadTime) {
         this.loadSuccessCount.increment();
         this.totalLoadTime.add(loadTime);
      }

      @Override
      public void recordLoadException(long loadTime) {
         this.loadExceptionCount.increment();
         this.totalLoadTime.add(loadTime);
      }

      @Override
      public void recordEviction() {
         this.evictionCount.increment();
      }

      @Override
      public CacheStats snapshot() {
         return new CacheStats(
            negativeToMaxValue(this.hitCount.sum()),
            negativeToMaxValue(this.missCount.sum()),
            negativeToMaxValue(this.loadSuccessCount.sum()),
            negativeToMaxValue(this.loadExceptionCount.sum()),
            negativeToMaxValue(this.totalLoadTime.sum()),
            negativeToMaxValue(this.evictionCount.sum())
         );
      }

      private static long negativeToMaxValue(long value) {
         return value >= 0L ? value : Long.MAX_VALUE;
      }

      public void incrementBy(AbstractCache.StatsCounter other) {
         CacheStats otherStats = other.snapshot();
         this.hitCount.add(otherStats.hitCount());
         this.missCount.add(otherStats.missCount());
         this.loadSuccessCount.add(otherStats.loadSuccessCount());
         this.loadExceptionCount.add(otherStats.loadExceptionCount());
         this.totalLoadTime.add(otherStats.totalLoadTime());
         this.evictionCount.add(otherStats.evictionCount());
      }
   }

   public interface StatsCounter {
      void recordHits(int count);

      void recordMisses(int count);

      void recordLoadSuccess(long loadTime);

      void recordLoadException(long loadTime);

      void recordEviction();

      CacheStats snapshot();
   }
}
