package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

class MapIteratorCache<K, V> {
   private final Map<K, V> backingMap;
   private transient volatile @Nullable Entry<K, V> cacheEntry;

   MapIteratorCache(Map<K, V> backingMap) {
      this.backingMap = Preconditions.checkNotNull(backingMap);
   }

   @CanIgnoreReturnValue
   final @Nullable V put(K key, V value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      this.clearCache();
      return this.backingMap.put(key, value);
   }

   @CanIgnoreReturnValue
   final @Nullable V remove(Object key) {
      Preconditions.checkNotNull(key);
      this.clearCache();
      return this.backingMap.remove(key);
   }

   final void clear() {
      this.clearCache();
      this.backingMap.clear();
   }

   @Nullable V get(Object key) {
      Preconditions.checkNotNull(key);
      V value = this.getIfCached(key);
      return value == null ? this.getWithoutCaching(key) : value;
   }

   final @Nullable V getWithoutCaching(Object key) {
      Preconditions.checkNotNull(key);
      return this.backingMap.get(key);
   }

   final boolean containsKey(@Nullable Object key) {
      return this.getIfCached(key) != null || this.backingMap.containsKey(key);
   }

   final Set<K> unmodifiableKeySet() {
      return new AbstractSet<K>() {
         public UnmodifiableIterator<K> iterator() {
            final Iterator<Entry<K, V>> entryIterator = MapIteratorCache.this.backingMap.entrySet().iterator();
            return new UnmodifiableIterator<K>() {
               @Override
               public boolean hasNext() {
                  return entryIterator.hasNext();
               }

               @Override
               public K next() {
                  Entry<K, V> entry = entryIterator.next();
                  MapIteratorCache.this.cacheEntry = entry;
                  return entry.getKey();
               }
            };
         }

         @Override
         public int size() {
            return MapIteratorCache.this.backingMap.size();
         }

         @Override
         public boolean contains(@Nullable Object key) {
            return MapIteratorCache.this.containsKey(key);
         }
      };
   }

   @Nullable V getIfCached(@Nullable Object key) {
      Entry<K, V> entry = this.cacheEntry;
      return entry != null && entry.getKey() == key ? entry.getValue() : null;
   }

   void clearCache() {
      this.cacheEntry = null;
   }
}
