package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

interface LocalCache<K, V> extends ConcurrentMap<K, V> {
   boolean isAsync();

   boolean isRecordingStats();

   StatsCounter statsCounter();

   void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause);

   Executor executor();

   ConcurrentMap<Object, CompletableFuture<?>> refreshes();

   @Nullable Expiry<K, V> expiry();

   Ticker statsTicker();

   long estimatedSize();

   Object referenceKey(K key);

   boolean isPendingEviction(K key);

   @Nullable V getIfPresent(K key, boolean recordStats);

   @Nullable V getIfPresentQuietly(Object key);

   Map<K, V> getAllPresent(Iterable<? extends K> keys);

   boolean replace(K key, V oldValue, V newValue, boolean shouldDiscardRefresh);

   @Override
   default @Nullable V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return this.compute(key, remappingFunction, this.expiry(), true, true);
   }

   @Nullable V compute(
      K key,
      BiFunction<? super K, ? super V, ? extends V> remappingFunction,
      @Nullable Expiry<? super K, ? super V> expiry,
      boolean recordLoad,
      boolean recordLoadFailure
   );

   @Override
   default @Nullable V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      return this.computeIfAbsent(key, mappingFunction, true, true);
   }

   @Nullable V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, boolean recordStats, boolean recordLoad);

   default void invalidateAll(Iterable<?> keys) {
      for (Object key : keys) {
         this.remove(key);
      }
   }

   void cleanUp();

   default void notifyOnReplace(K key, @Nullable V oldValue, V newValue) {
      if (oldValue != null && oldValue != newValue) {
         if (this.isAsync()) {
            CompletableFuture<?> oldFuture = (CompletableFuture<?>)oldValue;
            CompletableFuture<?> newFuture = (CompletableFuture<?>)newValue;
            newFuture.whenCompleteAsync((nv, e) -> {
               if (e == null) {
                  oldFuture.thenAcceptAsync(ov -> {
                     if (nv != ov) {
                        this.notifyRemoval(key, oldValue, RemovalCause.REPLACED);
                     }
                  }, this.executor());
               } else {
                  this.notifyRemoval(key, oldValue, RemovalCause.REPLACED);
               }
            }, this.executor());
         } else {
            this.notifyRemoval(key, oldValue, RemovalCause.REPLACED);
         }
      }
   }

   default <T, R> Function<? super T, ? extends R> statsAware(Function<? super T, ? extends R> mappingFunction, boolean recordLoad) {
      return !this.isRecordingStats() ? mappingFunction : key -> {
         this.statsCounter().recordMisses(1);
         long startTime = this.statsTicker().read();

         R value;
         try {
            value = (R)mappingFunction.apply(key);
         } catch (Throwable t) {
            this.statsCounter().recordLoadFailure(this.statsTicker().read() - startTime);
            throw t;
         }

         long loadTime = this.statsTicker().read() - startTime;
         if (recordLoad) {
            if (value == null) {
               this.statsCounter().recordLoadFailure(loadTime);
            } else {
               this.statsCounter().recordLoadSuccess(loadTime);
            }
         }

         return value;
      };
   }

   default <T, U, R> BiFunction<? super T, ? super U, ? extends R> statsAware(BiFunction<? super T, ? super U, ? extends R> remappingFunction) {
      return this.statsAware(remappingFunction, true, true);
   }

   default <T, U, R> BiFunction<? super T, ? super U, ? extends R> statsAware(
      BiFunction<? super T, ? super U, ? extends R> remappingFunction, boolean recordLoad, boolean recordLoadFailure
   ) {
      return !this.isRecordingStats() ? remappingFunction : (t, u) -> {
         long startTime = this.statsTicker().read();

         R result;
         try {
            result = (R)remappingFunction.apply(t, u);
         } catch (RuntimeException | Error e) {
            if (recordLoadFailure) {
               this.statsCounter().recordLoadFailure(this.statsTicker().read() - startTime);
            }

            throw e;
         }

         long loadTime = this.statsTicker().read() - startTime;
         if (recordLoad) {
            if (result == null) {
               this.statsCounter().recordLoadFailure(loadTime);
            } else {
               this.statsCounter().recordLoadSuccess(loadTime);
            }
         }

         return result;
      };
   }
}
