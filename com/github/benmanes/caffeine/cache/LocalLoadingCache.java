package com.github.benmanes.caffeine.cache;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

interface LocalLoadingCache<K, V> extends LocalManualCache<K, V>, LoadingCache<K, V> {
   Logger logger = System.getLogger(LocalLoadingCache.class.getName());

   AsyncCacheLoader<? super K, V> cacheLoader();

   Function<K, @Nullable V> mappingFunction();

   @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction();

   @Override
   default V get(K key) {
      return this.cache().computeIfAbsent(key, this.mappingFunction());
   }

   @Override
   default Map<K, V> getAll(Iterable<? extends K> keys) {
      Function<Set<? extends K>, Map<K, V>> mappingFunction = this.bulkMappingFunction();
      return mappingFunction == null ? this.loadSequentially(keys) : this.getAll(keys, mappingFunction);
   }

   default Map<K, V> loadSequentially(Iterable<? extends K> keys) {
      LinkedHashMap<K, V> result = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(keys));

      for (K key : keys) {
         result.put(key, null);
      }

      int count = 0;

      try {
         Iterator<Entry<K, V>> iter = result.entrySet().iterator();

         while (iter.hasNext()) {
            Entry<K, V> entry = iter.next();
            count++;
            V value = this.get(entry.getKey());
            if (value == null) {
               iter.remove();
            } else {
               entry.setValue(value);
            }
         }
      } catch (Throwable t) {
         this.cache().statsCounter().recordMisses(result.size() - count);
         throw t;
      }

      return Collections.unmodifiableMap(result);
   }

   @Override
   default CompletableFuture<V> refresh(K key) {
      long[] startTime = new long[1];
      V[] oldValue = (V[])(new Object[1]);
      CompletableFuture<? extends V>[] reloading = new CompletableFuture[1];
      Object keyReference = this.cache().referenceKey(key);
      CompletableFuture<?> future = this.cache()
         .refreshes()
         .compute(
            keyReference,
            (k, existing) -> {
               if (existing != null && !Async.isReady((CompletableFuture<?>)existing) && !this.cache().isPendingEviction(key)) {
                  return existing;
               }

               try {
                  startTime[0] = this.cache().statsTicker().read();
                  oldValue[0] = this.cache().getIfPresentQuietly(key);
                  CompletableFuture<? extends V> refreshFuture = oldValue[0] == null
                     ? this.cacheLoader().asyncLoad(key, this.cache().executor())
                     : this.cacheLoader().asyncReload(key, oldValue[0], this.cache().executor());
                  reloading[0] = Objects.requireNonNull(refreshFuture, "Null future");
                  return refreshFuture;
               } catch (RuntimeException e) {
                  throw e;
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  throw new CompletionException(e);
               } catch (Exception e) {
                  throw new CompletionException(e);
               }
            }
         );
      if (reloading[0] != null) {
         reloading[0].whenComplete((newValue, error) -> {
            long loadTime = this.cache().statsTicker().read() - startTime[0];
            if (error != null) {
               if (!(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
                  logger.log(Level.WARNING, "Exception thrown during refresh", error);
               }

               this.cache().refreshes().remove(keyReference, reloading[0]);
               this.cache().statsCounter().recordLoadFailure(loadTime);
            } else {
               boolean[] discard = new boolean[1];
               V value = this.cache().compute(key, (k, currentValue) -> {
                  boolean removed = this.cache().refreshes().remove(keyReference, reloading[0]);
                  if (removed && currentValue == oldValue[0]) {
                     return (V)(currentValue == null && newValue == null ? null : newValue);
                  }

                  discard[0] = currentValue != newValue;
                  return (V)currentValue;
               }, this.cache().expiry(), false, true);
               if (discard[0] && newValue != null) {
                  RemovalCause cause = value == null ? RemovalCause.EXPLICIT : RemovalCause.REPLACED;
                  this.cache().notifyRemoval(key, (V)newValue, cause);
               }

               if (newValue == null) {
                  this.cache().statsCounter().recordLoadFailure(loadTime);
               } else {
                  this.cache().statsCounter().recordLoadSuccess(loadTime);
               }
            }
         });
      }

      return (CompletableFuture<V>)future;
   }

   @Override
   default CompletableFuture<Map<K, V>> refreshAll(Iterable<? extends K> keys) {
      LinkedHashMap<K, CompletableFuture<V>> result = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(keys));

      for (K key : keys) {
         result.computeIfAbsent(key, this::refresh);
      }

      return LocalAsyncCache.composeResult(result);
   }

   static <K, V> Function<K, @Nullable V> newMappingFunction(CacheLoader<? super K, V> cacheLoader) {
      return key -> {
         try {
            return cacheLoader.load(key);
         } catch (RuntimeException e) {
            throw e;
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
         } catch (Exception e) {
            throw new CompletionException(e);
         }
      };
   }

   static <K, V> @Nullable Function<Set<? extends K>, Map<K, V>> newBulkMappingFunction(CacheLoader<? super K, V> cacheLoader) {
      return !hasLoadAll(cacheLoader) ? null : keysToLoad -> {
         try {
            return (Map<K, V>)cacheLoader.loadAll(keysToLoad);
         } catch (RuntimeException e) {
            throw e;
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
         } catch (Exception e) {
            throw new CompletionException(e);
         }
      };
   }

   static boolean hasLoadAll(CacheLoader<?, ?> cacheLoader) {
      return Caffeine.hasMethodOverride(CacheLoader.class, cacheLoader, "loadAll", Set.class);
   }
}
