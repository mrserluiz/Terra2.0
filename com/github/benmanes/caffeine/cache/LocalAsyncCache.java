package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

interface LocalAsyncCache<K, V> extends AsyncCache<K, V> {
   Logger logger = System.getLogger(LocalAsyncCache.class.getName());

   LocalCache<K, CompletableFuture<V>> cache();

   Policy<K, V> policy();

   @Override
   default @Nullable CompletableFuture<V> getIfPresent(K key) {
      return this.cache().getIfPresent(key, true);
   }

   @Override
   default CompletableFuture<V> get(K key, Function<? super K, ? extends V> mappingFunction) {
      return this.get(key, (k1, executor) -> CompletableFuture.supplyAsync(() -> mappingFunction.apply(key), executor));
   }

   @Override
   default CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction) {
      return this.get(key, mappingFunction, true);
   }

   default CompletableFuture<V> get(
      K key, BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction, boolean recordStats
   ) {
      long startTime = this.cache().statsTicker().read();
      CompletableFuture<? extends V>[] result = new CompletableFuture[1];
      CompletableFuture<V> future = this.cache().computeIfAbsent(key, k -> {
         CompletableFuture<V> castedResult = (CompletableFuture<V>)mappingFunction.apply(key, this.cache().executor());
         result[0] = castedResult;
         return Objects.requireNonNull(castedResult);
      }, recordStats, false);
      if (result[0] != null) {
         this.handleCompletion(key, result[0], startTime, false);
      }

      return Objects.requireNonNull(future);
   }

   @Override
   default CompletableFuture<Map<K, V>> getAll(
      Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction
   ) {
      return this.getAll(keys, (keysToLoad, executor) -> CompletableFuture.supplyAsync(() -> mappingFunction.apply(keysToLoad), executor));
   }

   @Override
   default CompletableFuture<Map<K, V>> getAll(
      Iterable<? extends K> keys,
      BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends V>>> mappingFunction
   ) {
      Objects.requireNonNull(mappingFunction);
      Objects.requireNonNull(keys);
      int initialCapacity = Caffeine.calculateHashMapCapacity(keys);
      LinkedHashMap<K, CompletableFuture<V>> futures = new LinkedHashMap<>(initialCapacity);
      LinkedHashMap<K, CompletableFuture<V>> proxies = new LinkedHashMap<>(initialCapacity);

      for (K key : keys) {
         if (!futures.containsKey(key)) {
            CompletableFuture<V> future = this.cache().getIfPresent(key, false);
            if (future == null) {
               CompletableFuture<V> proxy = new CompletableFuture<>();
               future = this.cache().putIfAbsent(key, proxy);
               if (future == null) {
                  future = proxy;
                  proxies.put(key, proxy);
               }
            }

            futures.put(key, future);
         }
      }

      this.cache().statsCounter().recordMisses(proxies.size());
      this.cache().statsCounter().recordHits(futures.size() - proxies.size());
      if (proxies.isEmpty()) {
         return composeResult(futures);
      }

      LocalAsyncCache.AsyncBulkCompleter<K, V> completer = new LocalAsyncCache.AsyncBulkCompleter<>(this.cache(), proxies);

      try {
         CompletableFuture<? extends Map<? extends K, ? extends V>> loader = (CompletableFuture<? extends Map<? extends K, ? extends V>>)mappingFunction.apply(
            Collections.unmodifiableSet(proxies.keySet()), this.cache().executor()
         );
         return loader.handle(completer).thenCompose(ignored -> composeResult(futures));
      } catch (Throwable t) {
         throw completer.error(t);
      }
   }

   static <K, V> CompletableFuture<Map<K, V>> composeResult(Map<K, CompletableFuture<V>> futures) {
      if (futures.isEmpty()) {
         Map<K, V> emptyMap = Collections.unmodifiableMap(Collections.emptyMap());
         return CompletableFuture.completedFuture(emptyMap);
      } else {
         CompletableFuture<?>[] array = futures.values().toArray(new CompletableFuture[0]);
         return CompletableFuture.allOf(array).thenApply(ignored -> {
            LinkedHashMap<K, V> result = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(futures.size()));
            futures.forEach((key, future) -> {
               V value = future.getNow(null);
               if (value != null) {
                  result.put((K)key, value);
               }
            });
            return Collections.unmodifiableMap(result);
         });
      }
   }

   @Override
   default void put(K key, CompletableFuture<? extends V> valueFuture) {
      if (!valueFuture.isCompletedExceptionally() && (!valueFuture.isDone() || valueFuture.join() != null)) {
         long startTime = this.cache().statsTicker().read();
         CompletableFuture<V> castedFuture = (CompletableFuture<V>)valueFuture;
         this.cache().put(key, castedFuture);
         this.handleCompletion(key, valueFuture, startTime, false);
      } else {
         this.cache().statsCounter().recordLoadFailure(0L);
         this.cache().remove(key);
      }
   }

   default void handleCompletion(K key, CompletableFuture<? extends V> valueFuture, long startTime, boolean recordMiss) {
      valueFuture.whenComplete((value, error) -> {
         long loadTime = this.cache().statsTicker().read() - startTime;
         if (value == null) {
            if (error != null && !(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
               logger.log(Level.WARNING, "Exception thrown during asynchronous load", error);
            }

            this.cache().statsCounter().recordLoadFailure(loadTime);
            this.cache().remove(key, valueFuture);
         } else {
            CompletableFuture<V> castedFuture = (CompletableFuture<V>)valueFuture;

            try {
               this.cache().replace(key, castedFuture, castedFuture, false);
               this.cache().statsCounter().recordLoadSuccess(loadTime);
            } catch (Throwable t) {
               logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
               this.cache().statsCounter().recordLoadFailure(loadTime);
               this.cache().remove(key, valueFuture);
            }
         }

         if (recordMiss) {
            this.cache().statsCounter().recordMisses(1);
         }
      });
   }

   abstract class AbstractCacheView<K, V> implements Cache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      transient @Nullable ConcurrentMap<K, V> asMapView;

      abstract LocalAsyncCache<K, V> asyncCache();

      @Override
      public @Nullable V getIfPresent(K key) {
         CompletableFuture<V> future = this.asyncCache().cache().getIfPresent(key, true);
         return Async.getIfReady(future);
      }

      @Override
      public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
         LinkedHashMap<K, V> result = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(keys));

         for (K key : keys) {
            result.put(key, null);
         }

         int uniqueKeys = result.size();
         Iterator<Entry<K, V>> iter = result.entrySet().iterator();

         while (iter.hasNext()) {
            Entry<K, V> entry = iter.next();
            CompletableFuture<V> future = this.asyncCache().cache().get(entry.getKey());
            V value = Async.getIfReady(future);
            if (value == null) {
               iter.remove();
            } else {
               entry.setValue(value);
            }
         }

         this.asyncCache().cache().statsCounter().recordHits(result.size());
         this.asyncCache().cache().statsCounter().recordMisses(uniqueKeys - result.size());
         return Collections.unmodifiableMap(result);
      }

      @Override
      public V get(K key, Function<? super K, ? extends V> mappingFunction) {
         return resolve(this.asyncCache().get(key, mappingFunction));
      }

      @Override
      public Map<K, V> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
         return resolve(this.asyncCache().getAll(keys, mappingFunction));
      }

      protected static <T> T resolve(CompletableFuture<T> future) {
         try {
            return future.join();
         } catch (LocalAsyncCache.AsyncBulkCompleter.NullMapCompletionException e) {
            throw new NullPointerException("null map");
         } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException) {
               throw (RuntimeException)e.getCause();
            } else if (e.getCause() instanceof Error) {
               throw (Error)e.getCause();
            } else {
               throw e;
            }
         }
      }

      @Override
      public void put(K key, V value) {
         Objects.requireNonNull(value);
         this.asyncCache().cache().put(key, CompletableFuture.completedFuture(value));
      }

      @Override
      public void putAll(Map<? extends K, ? extends V> map) {
         map.forEach(this::put);
      }

      @Override
      public void invalidate(K key) {
         this.asyncCache().cache().remove(key);
      }

      @Override
      public void invalidateAll(Iterable<? extends K> keys) {
         this.asyncCache().cache().invalidateAll(keys);
      }

      @Override
      public void invalidateAll() {
         this.asyncCache().cache().clear();
      }

      @Override
      public long estimatedSize() {
         return this.asyncCache().cache().estimatedSize();
      }

      @Override
      public CacheStats stats() {
         return this.asyncCache().cache().statsCounter().snapshot();
      }

      @Override
      public void cleanUp() {
         this.asyncCache().cache().cleanUp();
      }

      @Override
      public Policy<K, V> policy() {
         return this.asyncCache().policy();
      }

      @Override
      public ConcurrentMap<K, V> asMap() {
         return this.asMapView == null ? (this.asMapView = new LocalAsyncCache.AsMapView<>(this.asyncCache().cache())) : this.asMapView;
      }
   }

   final class AsMapView<K, V> implements ConcurrentMap<K, V> {
      final LocalCache<K, CompletableFuture<V>> delegate;
      @Nullable Set<K> keys;
      @Nullable Collection<V> values;
      @Nullable Set<Entry<K, V>> entries;

      AsMapView(LocalCache<K, CompletableFuture<V>> delegate) {
         this.delegate = delegate;
      }

      @Override
      public boolean isEmpty() {
         return this.delegate.isEmpty();
      }

      @Override
      public int size() {
         return this.delegate.size();
      }

      @Override
      public void clear() {
         this.delegate.clear();
      }

      @Override
      public boolean containsKey(Object key) {
         return Async.isReady(this.delegate.getIfPresentQuietly(key));
      }

      @Override
      public boolean containsValue(Object value) {
         Objects.requireNonNull(value);

         for (CompletableFuture<V> valueFuture : this.delegate.values()) {
            if (value.equals(Async.getIfReady(valueFuture))) {
               return true;
            }
         }

         return false;
      }

      @Override
      public @Nullable V get(Object key) {
         return Async.getIfReady(this.delegate.get(key));
      }

      @Override
      public @Nullable V putIfAbsent(K key, V value) {
         CompletableFuture<V> priorFuture = null;

         while (true) {
            priorFuture = priorFuture == null ? this.delegate.get(key) : this.delegate.getIfPresentQuietly(key);
            if (priorFuture != null) {
               if (!priorFuture.isDone()) {
                  Async.getWhenSuccessful(priorFuture);
                  continue;
               }

               V prior = Async.getWhenSuccessful(priorFuture);
               if (prior != null) {
                  return prior;
               }
            }

            boolean[] added = new boolean[]{false};
            CompletableFuture<V> computed = this.delegate.compute(key, (k, valueFuture) -> {
               added[0] = valueFuture == null || valueFuture.isDone() && Async.<V>getIfReady((CompletableFuture<V>)valueFuture) == null;
               return added[0] ? CompletableFuture.completedFuture(value) : valueFuture;
            }, this.delegate.expiry(), false, false);
            if (added[0]) {
               return null;
            }

            V prior = Async.getWhenSuccessful(computed);
            if (prior != null) {
               return prior;
            }
         }
      }

      @Override
      public void putAll(Map<? extends K, ? extends V> map) {
         map.forEach(this::put);
      }

      @Override
      public @Nullable V put(K key, V value) {
         Objects.requireNonNull(value);
         CompletableFuture<V> oldValueFuture = this.delegate.put(key, CompletableFuture.completedFuture(value));
         return Async.getWhenSuccessful(oldValueFuture);
      }

      @Override
      public @Nullable V remove(Object key) {
         CompletableFuture<V> oldValueFuture = this.delegate.remove(key);
         return Async.getWhenSuccessful(oldValueFuture);
      }

      @Override
      public boolean remove(Object key, Object value) {
         Objects.requireNonNull(key);
         if (value == null) {
            return false;
         }

         K castedKey = (K)key;
         boolean[] done = new boolean[]{false};
         boolean[] removed = new boolean[]{false};
         CompletableFuture<V> future = null;

         do {
            future = future == null ? this.delegate.get(castedKey) : this.delegate.getIfPresentQuietly(castedKey);
            if (future == null || future.isCompletedExceptionally()) {
               return false;
            }

            Async.getWhenSuccessful(future);
            this.delegate.compute(castedKey, (k, oldValueFuture) -> {
               if (oldValueFuture == null) {
                  done[0] = true;
                  return null;
               }

               if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               }

               done[0] = true;
               V oldValue = Async.getIfReady((CompletableFuture<V>)oldValueFuture);
               removed[0] = value.equals(oldValue);
               return oldValue != null && !removed[0] ? oldValueFuture : null;
            }, this.delegate.expiry(), false, true);
         } while (!done[0]);

         return removed[0];
      }

      @Override
      public V replace(K key, V value) {
         V[] oldValue = (V[])(new Object[1]);
         boolean[] done = new boolean[]{false};

         do {
            CompletableFuture<V> future = this.delegate.getIfPresentQuietly(key);
            if (future == null || future.isCompletedExceptionally()) {
               return null;
            }

            Async.getWhenSuccessful(future);
            this.delegate.compute(key, (k, oldValueFuture) -> {
               if (oldValueFuture == null) {
                  done[0] = true;
                  return null;
               }

               if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               }

               done[0] = true;
               oldValue[0] = Async.getIfReady((CompletableFuture<V>)oldValueFuture);
               return oldValue[0] == null ? null : CompletableFuture.completedFuture(value);
            }, this.delegate.expiry(), false, false);
         } while (!done[0]);

         return oldValue[0];
      }

      @Override
      public boolean replace(K key, V oldValue, V newValue) {
         Objects.requireNonNull(oldValue);
         boolean[] done = new boolean[]{false};
         boolean[] replaced = new boolean[]{false};

         do {
            CompletableFuture<V> future = this.delegate.getIfPresentQuietly(key);
            if (future == null || future.isCompletedExceptionally()) {
               return false;
            }

            Async.getWhenSuccessful(future);
            this.delegate.compute(key, (k, oldValueFuture) -> {
               if (oldValueFuture == null) {
                  done[0] = true;
                  return null;
               }

               if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               }

               done[0] = true;
               replaced[0] = oldValue.equals(Async.getIfReady((CompletableFuture<V>)oldValueFuture));
               return replaced[0] ? CompletableFuture.completedFuture(newValue) : oldValueFuture;
            }, this.delegate.expiry(), false, false);
         } while (!done[0]);

         return replaced[0];
      }

      @Override
      public @Nullable V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
         CompletableFuture<V> priorFuture = null;

         while (true) {
            while (true) {
               priorFuture = priorFuture == null ? this.delegate.get(key) : this.delegate.getIfPresentQuietly(key);
               if (priorFuture == null) {
                  break;
               }

               if (priorFuture.isDone()) {
                  V prior = Async.getWhenSuccessful(priorFuture);
                  if (prior != null) {
                     this.delegate.statsCounter().recordHits(1);
                     return prior;
                  }
                  break;
               }

               Async.getWhenSuccessful(priorFuture);
            }

            CompletableFuture<V>[] future = new CompletableFuture[1];
            CompletableFuture<V> computed = this.delegate.compute(key, (k, valueFuture) -> {
               if (valueFuture == null || valueFuture.isDone() && Async.<V>getIfReady((CompletableFuture<V>)valueFuture) == null) {
                  V newValue = (V)this.delegate.statsAware(mappingFunction, true).apply(key);
                  if (newValue == null) {
                     return null;
                  }

                  future[0] = CompletableFuture.completedFuture(newValue);
                  return future[0];
               } else {
                  return valueFuture;
               }
            }, this.delegate.expiry(), false, false);
            V result = Async.getWhenSuccessful(computed);
            if (computed == future[0] || result != null) {
               return result;
            }
         }
      }

      @Override
      public @Nullable V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
         V[] newValue = (V[])(new Object[1]);

         CompletableFuture<V> valueFuture;
         do {
            Async.getWhenSuccessful(this.delegate.getIfPresentQuietly(key));
            valueFuture = this.delegate.computeIfPresent(key, (k, oldValueFuture) -> {
               if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               }

               V oldValue = Async.getIfReady((CompletableFuture<V>)oldValueFuture);
               if (oldValue == null) {
                  return null;
               }

               newValue[0] = (V)remappingFunction.apply(key, oldValue);
               return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
            });
            if (newValue[0] != null) {
               return newValue[0];
            }
         } while (valueFuture != null);

         return null;
      }

      @Override
      public @Nullable V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
         V[] newValue = (V[])(new Object[1]);

         CompletableFuture<V> valueFuture;
         do {
            Async.getWhenSuccessful(this.delegate.getIfPresentQuietly(key));
            valueFuture = this.delegate.compute(key, (k, oldValueFuture) -> {
               if (oldValueFuture != null && !oldValueFuture.isDone()) {
                  return oldValueFuture;
               }

               V oldValue = Async.getIfReady((CompletableFuture<V>)oldValueFuture);
               BiFunction<? super K, ? super V, ? extends V> function = this.delegate.statsAware(remappingFunction, true, true);
               newValue[0] = (V)function.apply(key, oldValue);
               return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
            }, this.delegate.expiry(), false, false);
            if (newValue[0] != null) {
               return newValue[0];
            }
         } while (valueFuture != null);

         return null;
      }

      @Override
      public @Nullable V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
         Objects.requireNonNull(value);
         Objects.requireNonNull(remappingFunction);
         CompletableFuture<V> newValueFuture = CompletableFuture.completedFuture(value);
         boolean[] merged = new boolean[]{false};

         CompletableFuture<V> mergedValueFuture;
         do {
            Async.getWhenSuccessful(this.delegate.getIfPresentQuietly(key));
            mergedValueFuture = this.delegate.merge(key, newValueFuture, (oldValueFuture, valueFuture) -> {
               if (oldValueFuture != null && !oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  merged[0] = true;
                  V oldValue = Async.getIfReady((CompletableFuture<V>)oldValueFuture);
                  if (oldValue == null) {
                     return valueFuture;
                  } else {
                     V mergedValue = (V)remappingFunction.apply(oldValue, value);
                     if (mergedValue == null) {
                        return null;
                     } else if (mergedValue == oldValue) {
                        return oldValueFuture;
                     } else {
                        return mergedValue == value ? valueFuture : CompletableFuture.completedFuture(mergedValue);
                     }
                  }
               }
            });
         } while (!merged[0] && mergedValueFuture != newValueFuture);

         return Async.getWhenSuccessful(mergedValueFuture);
      }

      @Override
      public Set<K> keySet() {
         return this.keys == null ? (this.keys = new LocalAsyncCache.AsMapView.KeySet()) : this.keys;
      }

      @Override
      public Collection<V> values() {
         return this.values == null ? (this.values = new LocalAsyncCache.AsMapView.Values()) : this.values;
      }

      @Override
      public Set<Entry<K, V>> entrySet() {
         return this.entries == null ? (this.entries = new LocalAsyncCache.AsMapView.EntrySet()) : this.entries;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (o == this) {
            return true;
         }

         if (!(o instanceof Map)) {
            return false;
         }

         Map<?, ?> map = (Map<?, ?>)o;
         int expectedSize = this.size();
         if (map.size() != expectedSize) {
            return false;
         }

         int count = 0;

         for (LocalAsyncCache.AsMapView<K, V>.EntryIterator iterator = new LocalAsyncCache.AsMapView.EntryIterator(); iterator.hasNext(); count++) {
            Entry<K, V> entry = iterator.next();
            Object value = map.get(entry.getKey());
            if (value == null || value != entry.getValue() && !value.equals(entry.getValue())) {
               return false;
            }
         }

         return count == expectedSize;
      }

      @Override
      public int hashCode() {
         int hash = 0;
         LocalAsyncCache.AsMapView<K, V>.EntryIterator iterator = new LocalAsyncCache.AsMapView.EntryIterator();

         while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            hash += entry.hashCode();
         }

         return hash;
      }

      @Override
      public String toString() {
         StringBuilder result = new StringBuilder(50).append('{');
         LocalAsyncCache.AsMapView<K, V>.EntryIterator iterator = new LocalAsyncCache.AsMapView.EntryIterator();

         while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            result.append(entry.getKey() == this ? "(this Map)" : entry.getKey())
               .append('=')
               .append(entry.getValue() == this ? "(this Map)" : entry.getValue());
            if (iterator.hasNext()) {
               result.append(", ");
            }
         }

         return result.append('}').toString();
      }

      private final class EntryIterator implements Iterator<Entry<K, V>> {
         final Iterator<Entry<K, CompletableFuture<V>>> iterator = AsMapView.this.delegate.entrySet().iterator();
         @Nullable Entry<K, V> cursor;
         @Nullable Object removalKey;

         EntryIterator() {
         }

         @Override
         public boolean hasNext() {
            while (this.cursor == null && this.iterator.hasNext()) {
               Entry<K, CompletableFuture<V>> entry = this.iterator.next();
               V value = Async.getIfReady(entry.getValue());
               if (value != null) {
                  this.cursor = new WriteThroughEntry<>(AsMapView.this, entry.getKey(), value);
               }
            }

            return this.cursor != null;
         }

         public Entry<K, V> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            }

            K key = Objects.requireNonNull(this.cursor).getKey();
            Entry<K, V> entry = this.cursor;
            this.removalKey = key;
            this.cursor = null;
            return entry;
         }

         @Override
         public void remove() {
            Caffeine.requireState(this.removalKey != null);
            AsMapView.this.delegate.remove(this.removalKey);
            this.removalKey = null;
         }
      }

      private final class EntrySet extends AbstractSet<Entry<K, V>> {
         @Override
         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         @Override
         public int size() {
            return AsMapView.this.size();
         }

         @Override
         public void clear() {
            AsMapView.this.clear();
         }

         @Override
         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Entry<?, ?> entry = (Entry<?, ?>)o;
               Object key = entry.getKey();
               Object value = entry.getValue();
               if (key != null && value != null) {
                  V cachedValue = AsMapView.this.get(key);
                  return cachedValue != null && cachedValue.equals(value);
               } else {
                  return false;
               }
            }
         }

         @Override
         public boolean removeAll(Collection<?> collection) {
            boolean modified = false;
            if (collection instanceof Set && collection.size() > this.size()) {
               for (Entry<K, V> entry : this) {
                  if (collection.contains(entry)) {
                     modified |= this.remove(entry);
                  }
               }
            } else {
               for (Object o : collection) {
                  modified |= this.remove(o);
               }
            }

            return modified;
         }

         @Override
         public boolean remove(Object obj) {
            if (!(obj instanceof Entry)) {
               return false;
            }

            Entry<?, ?> entry = (Entry<?, ?>)obj;
            Object key = entry.getKey();
            return key != null && AsMapView.this.remove(key, entry.getValue());
         }

         @Override
         public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
            boolean modified = false;

            for (Entry<K, V> entry : this) {
               if (filter.test(entry)) {
                  modified |= AsMapView.this.remove(entry.getKey(), entry.getValue());
               }
            }

            return modified;
         }

         @Override
         public boolean retainAll(Collection<?> collection) {
            boolean modified = false;

            for (Entry<K, V> entry : this) {
               if (!collection.contains(entry) && this.remove(entry)) {
                  modified = true;
               }
            }

            return modified;
         }

         @Override
         public Iterator<Entry<K, V>> iterator() {
            return AsMapView.this.new EntryIterator();
         }
      }

      private final class KeySet extends AbstractSet<K> {
         @Override
         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         @Override
         public int size() {
            return AsMapView.this.size();
         }

         @Override
         public void clear() {
            AsMapView.this.clear();
         }

         @Override
         public boolean contains(Object o) {
            return AsMapView.this.containsKey(o);
         }

         @Override
         public boolean removeAll(Collection<?> collection) {
            return AsMapView.this.delegate.keySet().removeAll(collection);
         }

         @Override
         public boolean remove(Object o) {
            return AsMapView.this.delegate.keySet().remove(o);
         }

         @Override
         public boolean removeIf(Predicate<? super K> filter) {
            return AsMapView.this.delegate.keySet().removeIf(filter);
         }

         @Override
         public boolean retainAll(Collection<?> collection) {
            return AsMapView.this.delegate.keySet().retainAll(collection);
         }

         @Override
         public Iterator<K> iterator() {
            return new Iterator<K>() {
               final Iterator<Entry<K, V>> iterator = AsMapView.this.entrySet().iterator();

               @Override
               public boolean hasNext() {
                  return this.iterator.hasNext();
               }

               @Override
               public K next() {
                  return this.iterator.next().getKey();
               }

               @Override
               public void remove() {
                  this.iterator.remove();
               }
            };
         }
      }

      private final class Values extends AbstractCollection<V> {
         @Override
         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         @Override
         public int size() {
            return AsMapView.this.size();
         }

         @Override
         public void clear() {
            AsMapView.this.clear();
         }

         @Override
         public boolean contains(Object o) {
            return AsMapView.this.containsValue(o);
         }

         @Override
         public boolean removeAll(Collection<?> collection) {
            boolean modified = false;

            for (Entry<K, CompletableFuture<V>> entry : AsMapView.this.delegate.entrySet()) {
               V value = Async.getIfReady(entry.getValue());
               if (value != null && collection.contains(value) && AsMapView.this.remove(entry.getKey(), value)) {
                  modified = true;
               }
            }

            return modified;
         }

         @Override
         public boolean remove(Object o) {
            if (o == null) {
               return false;
            }

            for (Entry<K, CompletableFuture<V>> entry : AsMapView.this.delegate.entrySet()) {
               V value = Async.getIfReady(entry.getValue());
               if (value != null && value.equals(o) && AsMapView.this.remove(entry.getKey(), value)) {
                  return true;
               }
            }

            return false;
         }

         @Override
         public boolean removeIf(Predicate<? super V> filter) {
            return AsMapView.this.delegate.values().removeIf(future -> {
               V value = Async.getIfReady((CompletableFuture<V>)future);
               return value != null && filter.test(value);
            });
         }

         @Override
         public boolean retainAll(Collection<?> collection) {
            boolean modified = false;

            for (Entry<K, CompletableFuture<V>> entry : AsMapView.this.delegate.entrySet()) {
               V value = Async.getIfReady(entry.getValue());
               if (value != null && !collection.contains(value) && AsMapView.this.remove(entry.getKey(), value)) {
                  modified = true;
               }
            }

            return modified;
         }

         @Override
         public void forEach(Consumer<? super V> action) {
            AsMapView.this.delegate.values().forEach(future -> {
               V value = Async.getIfReady((CompletableFuture<V>)future);
               if (value != null) {
                  action.accept(value);
               }
            });
         }

         @Override
         public Iterator<V> iterator() {
            return new Iterator<V>() {
               final Iterator<Entry<K, V>> iterator = AsMapView.this.entrySet().iterator();

               @Override
               public boolean hasNext() {
                  return this.iterator.hasNext();
               }

               @Override
               public V next() {
                  return this.iterator.next().getValue();
               }

               @Override
               public void remove() {
                  this.iterator.remove();
               }
            };
         }
      }
   }

   final class AsyncAsMapView<K, V> implements ConcurrentMap<K, CompletableFuture<V>> {
      final LocalAsyncCache<K, V> asyncCache;

      AsyncAsMapView(LocalAsyncCache<K, V> asyncCache) {
         this.asyncCache = Objects.requireNonNull(asyncCache);
      }

      @Override
      public boolean isEmpty() {
         return this.asyncCache.cache().isEmpty();
      }

      @Override
      public int size() {
         return this.asyncCache.cache().size();
      }

      @Override
      public void clear() {
         this.asyncCache.cache().clear();
      }

      @Override
      public boolean containsKey(Object key) {
         return this.asyncCache.cache().containsKey(key);
      }

      @Override
      public boolean containsValue(Object value) {
         return this.asyncCache.cache().containsValue(value);
      }

      public @Nullable CompletableFuture<V> get(Object key) {
         return this.asyncCache.cache().get(key);
      }

      public CompletableFuture<V> putIfAbsent(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = this.asyncCache.cache().putIfAbsent(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (prior == null) {
            this.asyncCache.handleCompletion(key, value, startTime, false);
         }

         return prior;
      }

      public CompletableFuture<V> put(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = this.asyncCache.cache().put(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.handleCompletion(key, value, startTime, false);
         return prior;
      }

      @Override
      public void putAll(Map<? extends K, ? extends CompletableFuture<V>> map) {
         map.forEach(this::put);
      }

      public CompletableFuture<V> replace(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = this.asyncCache.cache().replace(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (prior != null) {
            this.asyncCache.handleCompletion(key, value, startTime, false);
         }

         return prior;
      }

      public boolean replace(K key, CompletableFuture<V> oldValue, CompletableFuture<V> newValue) {
         boolean replaced = this.asyncCache.cache().replace(key, oldValue, newValue);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (replaced) {
            this.asyncCache.handleCompletion(key, newValue, startTime, false);
         }

         return replaced;
      }

      public CompletableFuture<V> remove(Object key) {
         return this.asyncCache.cache().remove(key);
      }

      @Override
      public boolean remove(Object key, Object value) {
         return this.asyncCache.cache().remove(key, value);
      }

      public @Nullable CompletableFuture<V> computeIfAbsent(K key, Function<? super K, ? extends CompletableFuture<V>> mappingFunction) {
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         CompletableFuture<V> future = this.asyncCache.cache().computeIfAbsent(key, k -> {
            result[0] = (CompletableFuture<V>)mappingFunction.apply(k);
            return result[0];
         }, false, false);
         if (result[0] == null) {
            if (future != null && this.asyncCache.cache().isRecordingStats()) {
               future.whenComplete((r, e) -> {
                  if (r != null || e == null) {
                     this.asyncCache.cache().statsCounter().recordHits(1);
                  }
               });
            }
         } else {
            this.asyncCache.handleCompletion(key, result[0], startTime, true);
         }

         return future;
      }

      public CompletableFuture<V> computeIfPresent(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (k, oldValue) -> {
            result[0] = (CompletableFuture<V>)(oldValue == null ? null : remappingFunction.apply(k, oldValue));
            return result[0];
         }, this.asyncCache.cache().expiry(), false, false);
         if (result[0] != null) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public CompletableFuture<V> compute(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (k, oldValue) -> {
            result[0] = (CompletableFuture<V>)remappingFunction.apply(k, oldValue);
            return result[0];
         }, this.asyncCache.cache().expiry(), false, false);
         if (result[0] != null) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public CompletableFuture<V> merge(
         K key,
         CompletableFuture<V> value,
         BiFunction<? super CompletableFuture<V>, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction
      ) {
         Objects.requireNonNull(value);
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (k, oldValue) -> {
            result[0] = oldValue == null ? value : remappingFunction.apply(oldValue, value);
            return result[0];
         }, this.asyncCache.cache().expiry(), false, false);
         if (result[0] != null) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      @Override
      public void forEach(BiConsumer<? super K, ? super CompletableFuture<V>> action) {
         this.asyncCache.cache().forEach(action);
      }

      @Override
      public Set<K> keySet() {
         return this.asyncCache.cache().keySet();
      }

      @Override
      public Collection<CompletableFuture<V>> values() {
         return this.asyncCache.cache().values();
      }

      @Override
      public Set<Entry<K, CompletableFuture<V>>> entrySet() {
         return this.asyncCache.cache().entrySet();
      }

      @Override
      public boolean equals(@Nullable Object o) {
         return this.asyncCache.cache().equals(o);
      }

      @Override
      public int hashCode() {
         return this.asyncCache.cache().hashCode();
      }

      @Override
      public String toString() {
         return this.asyncCache.cache().toString();
      }
   }

   final class AsyncBulkCompleter<K, V> implements BiFunction<Map<? extends K, ? extends V>, Throwable, Map<? extends K, ? extends V>> {
      private final LocalCache<K, CompletableFuture<V>> cache;
      private final Map<K, CompletableFuture<V>> proxies;
      private final long startTime;

      AsyncBulkCompleter(LocalCache<K, CompletableFuture<V>> cache, Map<K, CompletableFuture<V>> proxies) {
         this.startTime = cache.statsTicker().read();
         this.proxies = proxies;
         this.cache = cache;
      }

      @CanIgnoreReturnValue
      public @Nullable Map<? extends K, ? extends V> apply(@Nullable Map<? extends K, ? extends V> result, @Nullable Throwable error) {
         long loadTime = this.cache.statsTicker().read() - this.startTime;
         Throwable failure = this.handleResponse(result, error);
         if (failure == null) {
            this.cache.statsCounter().recordLoadSuccess(loadTime);
            return result;
         } else {
            this.cache.statsCounter().recordLoadFailure(loadTime);
            if (failure instanceof RuntimeException) {
               throw (RuntimeException)failure;
            } else if (failure instanceof Error) {
               throw (Error)failure;
            } else {
               throw new CompletionException(failure);
            }
         }
      }

      public CompletionException error(Throwable error) {
         long loadTime = this.cache.statsTicker().read() - this.startTime;
         Throwable failure = this.handleResponse(null, error);
         this.cache.statsCounter().recordLoadFailure(loadTime);
         if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
         } else if (failure instanceof Error) {
            throw (Error)failure;
         } else {
            return new CompletionException(failure);
         }
      }

      private @Nullable Throwable handleResponse(@Nullable Map<? extends K, ? extends V> result, @Nullable Throwable error) {
         if (result != null) {
            Throwable failure = this.fillProxies(result);
            return this.addNewEntries(result, failure);
         }

         Throwable failure = error == null ? new LocalAsyncCache.AsyncBulkCompleter.NullMapCompletionException() : error;

         for (Entry<K, CompletableFuture<V>> entry : this.proxies.entrySet()) {
            this.cache.remove(entry.getKey(), entry.getValue());
            entry.getValue().obtrudeException(failure);
         }

         if (!(failure instanceof CancellationException) && !(failure instanceof TimeoutException)) {
            LocalAsyncCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", failure);
         }

         return failure;
      }

      private @Nullable Throwable fillProxies(Map<? extends K, ? extends V> result) {
         Throwable error = null;

         for (Entry<K, CompletableFuture<V>> entry : this.proxies.entrySet()) {
            K key = entry.getKey();
            V value = (V)result.get(key);
            CompletableFuture<V> future = entry.getValue();
            future.obtrudeValue(value);
            if (value == null) {
               this.cache.remove(key, future);
            } else {
               try {
                  this.cache.replace(key, future, future);
               } catch (Throwable t) {
                  LocalAsyncCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
                  this.cache.remove(key, future);
                  if (error == null) {
                     error = t;
                  } else {
                     error.addSuppressed(t);
                  }
               }
            }
         }

         return error;
      }

      private @Nullable Throwable addNewEntries(Map<? extends K, ? extends V> result, @Nullable Throwable failure) {
         Throwable error = failure;

         for (Entry<? extends K, ? extends V> entry : result.entrySet()) {
            K key = (K)entry.getKey();
            V value = (V)result.get(key);
            if (!this.proxies.containsKey(key)) {
               try {
                  this.cache.put(key, CompletableFuture.completedFuture(value));
               } catch (Throwable t) {
                  LocalAsyncCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
                  if (error == null) {
                     error = t;
                  } else {
                     error.addSuppressed(t);
                  }
               }
            }
         }

         return error;
      }

      static final class NullMapCompletionException extends CompletionException {
         private static final long serialVersionUID = 1L;
      }
   }

   final class CacheView<K, V> extends LocalAsyncCache.AbstractCacheView<K, V> {
      private static final long serialVersionUID = 1L;
      final LocalAsyncCache<K, V> asyncCache;

      CacheView(LocalAsyncCache<K, V> asyncCache) {
         this.asyncCache = Objects.requireNonNull(asyncCache);
      }

      @Override
      LocalAsyncCache<K, V> asyncCache() {
         return this.asyncCache;
      }
   }
}
