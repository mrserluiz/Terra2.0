package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

final class UnboundedLocalCache<K, V> implements LocalCache<K, V> {
   static final Logger logger = System.getLogger(UnboundedLocalCache.class.getName());
   static final VarHandle REFRESHES = findVarHandle(UnboundedLocalCache.class, "refreshes", ConcurrentMap.class);
   final @Nullable RemovalListener<K, V> removalListener;
   final ConcurrentHashMap<K, V> data;
   final StatsCounter statsCounter;
   final boolean isRecordingStats;
   final Executor executor;
   final boolean isAsync;
   @Nullable Set<K> keySet;
   @Nullable Collection<V> values;
   @Nullable Set<Entry<K, V>> entrySet;
   volatile @Nullable ConcurrentMap<Object, CompletableFuture<?>> refreshes;

   UnboundedLocalCache(Caffeine<? super K, ? super V> builder, boolean isAsync) {
      this.data = new ConcurrentHashMap<>(builder.getInitialCapacity());
      this.statsCounter = builder.getStatsCounterSupplier().get();
      this.removalListener = builder.getRemovalListener(isAsync);
      this.isRecordingStats = builder.isRecordingStats();
      this.executor = builder.getExecutor();
      this.isAsync = isAsync;
   }

   static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
      try {
         return MethodHandles.lookup().findVarHandle(recv, name, type);
      } catch (ReflectiveOperationException e) {
         throw new ExceptionInInitializerError(e);
      }
   }

   @Override
   public boolean isAsync() {
      return this.isAsync;
   }

   @Override
   public @Nullable Expiry<K, V> expiry() {
      return null;
   }

   @CanIgnoreReturnValue
   @Override
   public Object referenceKey(K key) {
      return key;
   }

   @Override
   public boolean isPendingEviction(K key) {
      return false;
   }

   @Override
   public @Nullable V getIfPresent(Object key, boolean recordStats) {
      V value = this.data.get(key);
      if (recordStats) {
         if (value == null) {
            this.statsCounter.recordMisses(1);
         } else {
            this.statsCounter.recordHits(1);
         }
      }

      return value;
   }

   @Override
   public @Nullable V getIfPresentQuietly(Object key) {
      return this.data.get(key);
   }

   @Override
   public long estimatedSize() {
      return this.data.mappingCount();
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
         V value = this.data.get(entry.getKey());
         if (value == null) {
            iter.remove();
         } else {
            entry.setValue(value);
         }
      }

      this.statsCounter.recordHits(result.size());
      this.statsCounter.recordMisses(uniqueKeys - result.size());
      return Collections.unmodifiableMap(result);
   }

   @Override
   public void cleanUp() {
   }

   @Override
   public StatsCounter statsCounter() {
      return this.statsCounter;
   }

   @Override
   public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
      if (this.removalListener != null) {
         Runnable task = () -> {
            try {
               this.removalListener.onRemoval(key, value, cause);
            } catch (Throwable t) {
               logger.log(Level.WARNING, "Exception thrown by removal listener", t);
            }
         };

         try {
            this.executor.execute(task);
         } catch (Throwable t) {
            logger.log(Level.ERROR, "Exception thrown when submitting removal listener", t);
            task.run();
         }
      }
   }

   @Override
   public boolean isRecordingStats() {
      return this.isRecordingStats;
   }

   @Override
   public Executor executor() {
      return this.executor;
   }

   @Override
   public ConcurrentMap<Object, CompletableFuture<?>> refreshes() {
      ConcurrentMap<Object, CompletableFuture<?>> pending = this.refreshes;
      if (pending == null) {
         pending = new ConcurrentHashMap<>();
         if (!REFRESHES.compareAndSet((UnboundedLocalCache)this, (Void)null, (ConcurrentMap)pending)) {
            pending = Objects.requireNonNull(this.refreshes);
         }
      }

      return pending;
   }

   void discardRefresh(Object keyReference) {
      ConcurrentMap<Object, CompletableFuture<?>> pending = this.refreshes;
      if (pending != null) {
         pending.remove(keyReference);
      }
   }

   @Override
   public Ticker statsTicker() {
      return this.isRecordingStats ? Ticker.systemTicker() : Ticker.disabledTicker();
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      this.data.forEach(action);
   }

   @Override
   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      K[] notificationKey = (K[])(new Object[1]);
      V[] notificationValue = (V[])(new Object[1]);
      this.data.replaceAll((key, value) -> {
         if (notificationKey[0] != null) {
            this.notifyRemoval(notificationKey[0], notificationValue[0], RemovalCause.REPLACED);
            notificationValue[0] = null;
            notificationKey[0] = null;
         }

         V newValue = Objects.requireNonNull((V)function.apply(key, value));
         if (newValue != value) {
            notificationKey[0] = (K)key;
            notificationValue[0] = (V)value;
         }

         return newValue;
      });
      if (notificationKey[0] != null) {
         this.notifyRemoval(notificationKey[0], notificationValue[0], RemovalCause.REPLACED);
      }
   }

   @Override
   public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, boolean recordStats, boolean recordLoad) {
      Objects.requireNonNull(mappingFunction);
      V value = this.data.get(key);
      if (value != null) {
         if (recordStats) {
            this.statsCounter.recordHits(1);
         }

         return value;
      } else {
         boolean[] missed = new boolean[1];
         value = this.data.computeIfAbsent(key, k -> {
            missed[0] = true;
            return recordStats ? this.statsAware(mappingFunction, recordLoad).apply(key) : mappingFunction.apply(key);
         });
         if (!missed[0] && recordStats) {
            this.statsCounter.recordHits(1);
         }

         return value;
      }
   }

   @Override
   public @Nullable V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      if (!this.data.containsKey(key)) {
         return null;
      }

      V[] oldValue = (V[])(new Object[1]);
      boolean[] replaced = new boolean[1];
      V nv = this.data.computeIfPresent(key, (k, value) -> {
         BiFunction<? super K, ? super V, ? extends V> function = this.statsAware(remappingFunction, true, true);
         V newValue = (V)function.apply(k, value);
         replaced[0] = newValue != null;
         if (newValue != value) {
            oldValue[0] = (V)value;
         }

         this.discardRefresh(k);
         return newValue;
      });
      if (replaced[0]) {
         this.notifyOnReplace(key, oldValue[0], nv);
      } else if (oldValue[0] != null) {
         this.notifyRemoval(key, oldValue[0], RemovalCause.EXPLICIT);
      }

      return nv;
   }

   @Override
   public V compute(
      K key,
      BiFunction<? super K, ? super V, ? extends V> remappingFunction,
      @Nullable Expiry<? super K, ? super V> expiry,
      boolean recordLoad,
      boolean recordLoadFailure
   ) {
      Objects.requireNonNull(remappingFunction);
      return this.remap(key, this.statsAware(remappingFunction, recordLoad, recordLoadFailure));
   }

   @Override
   public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      Objects.requireNonNull(value);
      return this.remap(key, (k, oldValue) -> oldValue == null ? value : this.statsAware(remappingFunction).apply(oldValue, value));
   }

   V remap(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      V[] oldValue = (V[])(new Object[1]);
      boolean[] replaced = new boolean[1];
      V nv = this.data.compute(key, (k, value) -> {
         V newValue = (V)remappingFunction.apply(k, value);
         if (value == null && newValue == null) {
            return null;
         }

         replaced[0] = newValue != null;
         if (newValue != value) {
            oldValue[0] = (V)value;
         }

         this.discardRefresh(k);
         return newValue;
      });
      if (replaced[0]) {
         this.notifyOnReplace(key, oldValue[0], nv);
      } else if (oldValue[0] != null) {
         this.notifyRemoval(key, oldValue[0], RemovalCause.EXPLICIT);
      }

      return nv;
   }

   @Override
   public boolean isEmpty() {
      return this.data.isEmpty();
   }

   @Override
   public int size() {
      return this.data.size();
   }

   @Override
   public void clear() {
      if (this.removalListener != null || this.refreshes != null && !this.refreshes.isEmpty()) {
         for (K key : List.copyOf(this.data.keySet())) {
            this.remove(key);
         }
      } else {
         this.data.clear();
      }
   }

   @Override
   public boolean containsKey(Object key) {
      return this.data.containsKey(key);
   }

   @Override
   public boolean containsValue(Object value) {
      return this.data.containsValue(value);
   }

   @Override
   public @Nullable V get(Object key) {
      return this.getIfPresent(key, false);
   }

   @Override
   public @Nullable V put(K key, V value) {
      V oldValue = this.data.put(key, value);
      this.notifyOnReplace(key, oldValue, value);
      return oldValue;
   }

   @Override
   public @Nullable V putIfAbsent(K key, V value) {
      return this.data.putIfAbsent(key, value);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      if (this.removalListener == null) {
         this.data.putAll(map);
      } else {
         map.forEach(this::put);
      }
   }

   @Override
   public @Nullable V remove(Object key) {
      K castKey = (K)key;
      V[] oldValue = (V[])(new Object[1]);
      this.data.computeIfPresent(castKey, (k, v) -> {
         this.discardRefresh(k);
         oldValue[0] = (V)v;
         return null;
      });
      if (oldValue[0] != null) {
         this.notifyRemoval(castKey, oldValue[0], RemovalCause.EXPLICIT);
      }

      return oldValue[0];
   }

   @Override
   public boolean remove(Object key, Object value) {
      if (value == null) {
         return false;
      } else {
         K castKey = (K)key;
         V[] oldValue = (V[])(new Object[1]);
         this.data.computeIfPresent(castKey, (k, v) -> {
            if (v.equals(value)) {
               this.discardRefresh(k);
               oldValue[0] = (V)v;
               return null;
            } else {
               return (V)v;
            }
         });
         if (oldValue[0] != null) {
            this.notifyRemoval(castKey, oldValue[0], RemovalCause.EXPLICIT);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public @Nullable V replace(K key, V value) {
      V[] oldValue = (V[])(new Object[1]);
      this.data.computeIfPresent(key, (k, v) -> {
         this.discardRefresh(k);
         oldValue[0] = (V)v;
         return value;
      });
      if (oldValue[0] != null && oldValue[0] != value) {
         this.notifyRemoval(key, oldValue[0], RemovalCause.REPLACED);
      }

      return oldValue[0];
   }

   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      return this.replace(key, oldValue, newValue, true);
   }

   @Override
   public boolean replace(K key, V oldValue, V newValue, boolean shouldDiscardRefresh) {
      Objects.requireNonNull(oldValue);
      V[] prev = (V[])(new Object[1]);
      this.data.computeIfPresent(key, (k, v) -> {
         if (v.equals(oldValue)) {
            if (shouldDiscardRefresh) {
               this.discardRefresh(k);
            }

            prev[0] = (V)v;
            return newValue;
         } else {
            return (V)v;
         }
      });
      boolean replaced = prev[0] != null;
      if (replaced && prev[0] != newValue) {
         this.notifyRemoval(key, prev[0], RemovalCause.REPLACED);
      }

      return replaced;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      return o == this || this.data.equals(o);
   }

   @Override
   public int hashCode() {
      return this.data.hashCode();
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder(50).append('{');
      this.data.forEach((key, value) -> {
         if (result.length() != 1) {
            result.append(", ");
         }

         result.append(key == this ? "(this Map)" : key).append('=').append(value == this ? "(this Map)" : value);
      });
      return result.append('}').toString();
   }

   @Override
   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      return ks == null ? (this.keySet = new UnboundedLocalCache.KeySetView<>(this)) : ks;
   }

   @Override
   public Collection<V> values() {
      Collection<V> vs = this.values;
      return vs == null ? (this.values = new UnboundedLocalCache.ValuesView<>(this)) : vs;
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> es = this.entrySet;
      return es == null ? (this.entrySet = new UnboundedLocalCache.EntrySetView<>(this)) : es;
   }

   static final class EntryIterator<K, V> implements Iterator<Entry<K, V>> {
      final UnboundedLocalCache<K, V> cache;
      final Iterator<Entry<K, V>> iterator;
      @Nullable Entry<K, V> entry;

      EntryIterator(UnboundedLocalCache<K, V> cache) {
         this.iterator = cache.data.entrySet().iterator();
         this.cache = cache;
      }

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public Entry<K, V> next() {
         this.entry = this.iterator.next();
         return new WriteThroughEntry<>(this.cache, this.entry.getKey(), this.entry.getValue());
      }

      @Override
      public void remove() {
         if (this.entry == null) {
            throw new IllegalStateException();
         }

         this.cache.remove(this.entry.getKey());
         this.entry = null;
      }
   }

   static final class EntrySetView<K, V> extends AbstractSet<Entry<K, V>> {
      final UnboundedLocalCache<K, V> cache;

      EntrySetView(UnboundedLocalCache<K, V> cache) {
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public boolean isEmpty() {
         return this.cache.isEmpty();
      }

      @Override
      public int size() {
         return this.cache.size();
      }

      @Override
      public void clear() {
         this.cache.clear();
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
               V cachedValue = this.cache.get(key);
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
      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         }

         Entry<?, ?> entry = (Entry<?, ?>)o;
         Object key = entry.getKey();
         return key != null && this.cache.remove(key, entry.getValue());
      }

      @Override
      public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
         boolean removed = false;

         for (Entry<K, V> entry : this.cache.data.entrySet()) {
            if (filter.test(entry)) {
               removed |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return removed;
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
         return new UnboundedLocalCache.EntryIterator<>(this.cache);
      }

      @Override
      public Spliterator<Entry<K, V>> spliterator() {
         return new UnboundedLocalCache.EntrySpliterator<>(this.cache);
      }
   }

   static final class EntrySpliterator<K, V> implements Spliterator<Entry<K, V>> {
      final Spliterator<Entry<K, V>> spliterator;
      final UnboundedLocalCache<K, V> cache;

      EntrySpliterator(UnboundedLocalCache<K, V> cache) {
         this(cache, cache.data.entrySet().spliterator());
      }

      EntrySpliterator(UnboundedLocalCache<K, V> cache, Spliterator<Entry<K, V>> spliterator) {
         this.spliterator = Objects.requireNonNull(spliterator);
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
         Objects.requireNonNull(action);
         this.spliterator.forEachRemaining(entry -> {
            WriteThroughEntry<K, V> e = new WriteThroughEntry<>(this.cache, entry.getKey(), entry.getValue());
            action.accept(e);
         });
      }

      @Override
      public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
         Objects.requireNonNull(action);
         return this.spliterator.tryAdvance(entry -> {
            WriteThroughEntry<K, V> e = new WriteThroughEntry<>(this.cache, entry.getKey(), entry.getValue());
            action.accept(e);
         });
      }

      public UnboundedLocalCache.@Nullable EntrySpliterator<K, V> trySplit() {
         Spliterator<Entry<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new UnboundedLocalCache.EntrySpliterator<>(this.cache, split);
      }

      @Override
      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      @Override
      public int characteristics() {
         return this.spliterator.characteristics();
      }
   }

   static final class KeyIterator<K> implements Iterator<K> {
      final UnboundedLocalCache<K, ?> cache;
      final Iterator<K> iterator;
      @Nullable K current;

      KeyIterator(UnboundedLocalCache<K, ?> cache) {
         this.iterator = cache.data.keySet().iterator();
         this.cache = cache;
      }

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      @Override
      public K next() {
         this.current = this.iterator.next();
         return this.current;
      }

      @Override
      public void remove() {
         if (this.current == null) {
            throw new IllegalStateException();
         }

         this.cache.remove(this.current);
         this.current = null;
      }
   }

   static final class KeySetView<K> extends AbstractSet<K> {
      final UnboundedLocalCache<K, ?> cache;

      KeySetView(UnboundedLocalCache<K, ?> cache) {
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public boolean isEmpty() {
         return this.cache.isEmpty();
      }

      @Override
      public int size() {
         return this.cache.size();
      }

      @Override
      public void clear() {
         this.cache.clear();
      }

      @Override
      public boolean contains(Object o) {
         return this.cache.containsKey(o);
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean modified = false;
         if (collection instanceof Set && collection.size() > this.size()) {
            for (K key : this) {
               if (collection.contains(key)) {
                  modified |= this.remove(key);
               }
            }
         } else {
            for (Object o : collection) {
               modified |= o != null && this.remove(o);
            }
         }

         return modified;
      }

      @Override
      public boolean remove(Object o) {
         return this.cache.remove(o) != null;
      }

      @Override
      public boolean removeIf(Predicate<? super K> filter) {
         boolean modified = false;

         for (K key : this) {
            if (filter.test(key) && this.remove(key)) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;

         for (K key : this) {
            if (!collection.contains(key) && this.remove(key)) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public void forEach(Consumer<? super K> action) {
         this.cache.data.keySet().forEach(action);
      }

      @Override
      public Iterator<K> iterator() {
         return new UnboundedLocalCache.KeyIterator<>(this.cache);
      }

      @Override
      public Spliterator<K> spliterator() {
         return this.cache.data.keySet().spliterator();
      }

      @Override
      public Object[] toArray() {
         return this.cache.data.keySet().toArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
         return (T[])this.cache.data.keySet().toArray(array);
      }
   }

   static final class UnboundedLocalAsyncCache<K, V> implements LocalAsyncCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, CompletableFuture<V>> cache;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      LocalAsyncCache.@Nullable CacheView<K, V> cacheView;
      @Nullable Policy<K, V> policy;

      UnboundedLocalAsyncCache(Caffeine<K, V> builder) {
         this.cache = new UnboundedLocalCache<>(builder, true);
      }

      public UnboundedLocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      @Override
      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<>(this)) : this.mapView;
      }

      @Override
      public Cache<K, V> synchronous() {
         return this.cacheView == null ? (this.cacheView = new LocalAsyncCache.CacheView<>(this)) : this.cacheView;
      }

      @Override
      public Policy<K, V> policy() {
         UnboundedLocalCache<K, V> castCache = this.cache;
         Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
         Function<V, V> castTransformer = transformer;
         return this.policy == null ? (this.policy = new UnboundedLocalCache.UnboundedPolicy<>(castCache, castTransformer)) : this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<>();
         proxy.isRecordingStats = this.cache.isRecordingStats;
         proxy.removalListener = this.cache.removalListener;
         proxy.async = true;
         return proxy;
      }
   }

   static final class UnboundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<K, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, CompletableFuture<V>> cache;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      @Nullable Policy<K, V> policy;

      UnboundedLocalAsyncLoadingCache(Caffeine<K, V> builder, AsyncCacheLoader<? super K, V> loader) {
         super(loader);
         this.cache = new UnboundedLocalCache<>(builder, true);
      }

      @Override
      public LocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      @Override
      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<>(this)) : this.mapView;
      }

      @Override
      public Policy<K, V> policy() {
         UnboundedLocalCache<K, V> castCache = this.cache;
         Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
         Function<V, V> castTransformer = transformer;
         return this.policy == null ? (this.policy = new UnboundedLocalCache.UnboundedPolicy<>(castCache, castTransformer)) : this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<>();
         proxy.isRecordingStats = this.cache.isRecordingStats();
         proxy.removalListener = this.cache.removalListener;
         proxy.cacheLoader = this.cacheLoader;
         proxy.async = true;
         return proxy;
      }
   }

   static final class UnboundedLocalLoadingCache<K, V> extends UnboundedLocalCache.UnboundedLocalManualCache<K, V> implements LocalLoadingCache<K, V> {
      private static final long serialVersionUID = 1L;
      final Function<K, @Nullable V> mappingFunction;
      final CacheLoader<? super K, V> cacheLoader;
      final @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction;

      UnboundedLocalLoadingCache(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader) {
         super(builder);
         this.cacheLoader = cacheLoader;
         this.mappingFunction = LocalLoadingCache.newMappingFunction(cacheLoader);
         this.bulkMappingFunction = LocalLoadingCache.newBulkMappingFunction(cacheLoader);
      }

      @Override
      public AsyncCacheLoader<? super K, V> cacheLoader() {
         return this.cacheLoader;
      }

      @Override
      public Function<K, @Nullable V> mappingFunction() {
         return this.mappingFunction;
      }

      @Override
      public @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction() {
         return this.bulkMappingFunction;
      }

      @Override
      Object writeReplace() {
         SerializationProxy<K, V> proxy = (SerializationProxy<K, V>)super.writeReplace();
         proxy.cacheLoader = this.cacheLoader;
         return proxy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }
   }

   static class UnboundedLocalManualCache<K, V> implements LocalManualCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, V> cache;
      @Nullable Policy<K, V> policy;

      UnboundedLocalManualCache(Caffeine<K, V> builder) {
         this.cache = new UnboundedLocalCache<>(builder, false);
      }

      public final UnboundedLocalCache<K, V> cache() {
         return this.cache;
      }

      @Override
      public final Policy<K, V> policy() {
         if (this.policy == null) {
            Function<V, V> identity = v -> v;
            this.policy = new UnboundedLocalCache.UnboundedPolicy<>(this.cache, identity);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<>();
         proxy.isRecordingStats = this.cache.isRecordingStats;
         proxy.removalListener = this.cache.removalListener;
         return proxy;
      }
   }

   static final class UnboundedPolicy<K, V> implements Policy<K, V> {
      final Function<@Nullable V, @Nullable V> transformer;
      final UnboundedLocalCache<K, V> cache;

      UnboundedPolicy(UnboundedLocalCache<K, V> cache, Function<@Nullable V, @Nullable V> transformer) {
         this.transformer = transformer;
         this.cache = cache;
      }

      @Override
      public boolean isRecordingStats() {
         return this.cache.isRecordingStats;
      }

      @Override
      public @Nullable V getIfPresentQuietly(K key) {
         return this.transformer.apply(this.cache.data.get(key));
      }

      @Override
      public Policy.@Nullable CacheEntry<K, V> getEntryIfPresentQuietly(K key) {
         V value = this.transformer.apply(this.cache.data.get(key));
         return value == null ? null : SnapshotEntry.forEntry(key, value);
      }

      @Override
      public Map<K, CompletableFuture<V>> refreshes() {
         ConcurrentMap<Object, CompletableFuture<?>> refreshes = this.cache.refreshes;
         if (refreshes != null && !refreshes.isEmpty()) {
            Map<K, CompletableFuture<V>> castedRefreshes = (Map<K, CompletableFuture<V>>)refreshes;
            return Collections.unmodifiableMap(new HashMap<>(castedRefreshes));
         } else {
            return Collections.unmodifiableMap(Collections.emptyMap());
         }
      }

      @Override
      public Optional<Policy.Eviction<K, V>> eviction() {
         return Optional.empty();
      }

      @Override
      public Optional<Policy.FixedExpiration<K, V>> expireAfterAccess() {
         return Optional.empty();
      }

      @Override
      public Optional<Policy.FixedExpiration<K, V>> expireAfterWrite() {
         return Optional.empty();
      }

      @Override
      public Optional<Policy.VarExpiration<K, V>> expireVariably() {
         return Optional.empty();
      }

      @Override
      public Optional<Policy.FixedRefresh<K, V>> refreshAfterWrite() {
         return Optional.empty();
      }
   }

   static final class ValuesIterator<K, V> implements Iterator<V> {
      final UnboundedLocalCache<K, V> cache;
      final Iterator<Entry<K, V>> iterator;
      @Nullable Entry<K, V> entry;

      ValuesIterator(UnboundedLocalCache<K, V> cache) {
         this.iterator = cache.data.entrySet().iterator();
         this.cache = cache;
      }

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      @Override
      public V next() {
         this.entry = this.iterator.next();
         return this.entry.getValue();
      }

      @Override
      public void remove() {
         if (this.entry == null) {
            throw new IllegalStateException();
         }

         this.cache.remove(this.entry.getKey());
         this.entry = null;
      }
   }

   static final class ValuesView<K, V> extends AbstractCollection<V> {
      final UnboundedLocalCache<K, V> cache;

      ValuesView(UnboundedLocalCache<K, V> cache) {
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public boolean isEmpty() {
         return this.cache.isEmpty();
      }

      @Override
      public int size() {
         return this.cache.size();
      }

      @Override
      public void clear() {
         this.cache.clear();
      }

      @Override
      public boolean contains(Object o) {
         return this.cache.containsValue(o);
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean modified = false;

         for (Entry<K, V> entry : this.cache.data.entrySet()) {
            if (collection.contains(entry.getValue()) && this.cache.remove(entry.getKey(), entry.getValue())) {
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

         for (Entry<K, V> entry : this.cache.data.entrySet()) {
            if (o.equals(entry.getValue()) && this.cache.remove(entry.getKey(), entry.getValue())) {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean removeIf(Predicate<? super V> filter) {
         boolean removed = false;

         for (Entry<K, V> entry : this.cache.data.entrySet()) {
            if (filter.test(entry.getValue())) {
               removed |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return removed;
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;

         for (Entry<K, V> entry : this.cache.data.entrySet()) {
            if (!collection.contains(entry.getValue()) && this.cache.remove(entry.getKey(), entry.getValue())) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         this.cache.data.values().forEach(action);
      }

      @Override
      public Iterator<V> iterator() {
         return new UnboundedLocalCache.ValuesIterator<>(this.cache);
      }

      @Override
      public Spliterator<V> spliterator() {
         return this.cache.data.values().spliterator();
      }

      @Override
      public Object[] toArray() {
         return this.cache.data.values().toArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
         return (T[])this.cache.data.values().toArray(array);
      }
   }
}
