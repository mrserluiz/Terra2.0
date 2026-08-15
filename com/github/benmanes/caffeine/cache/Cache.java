package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Cache<K, V> {
   @Nullable V getIfPresent(K key);

   V get(K key, Function<? super K, ? extends V> mappingFunction);

   Map<K, @NonNull V> getAllPresent(Iterable<? extends K> keys);

   Map<K, @NonNull V> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction);

   void put(K key, @NonNull V value);

   void putAll(Map<? extends K, ? extends @NonNull V> map);

   void invalidate(K key);

   void invalidateAll(Iterable<? extends K> keys);

   void invalidateAll();

   long estimatedSize();

   CacheStats stats();

   ConcurrentMap<K, @NonNull V> asMap();

   void cleanUp();

   Policy<K, @NonNull V> policy();
}
