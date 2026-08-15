package com.github.benmanes.caffeine.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface AsyncCache<K, V> {
   @Nullable CompletableFuture<@NonNull V> getIfPresent(K key);

   CompletableFuture<V> get(K key, Function<? super @NonNull K, ? extends V> mappingFunction);

   CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction);

   CompletableFuture<Map<K, @NonNull V>> getAll(
      Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction
   );

   CompletableFuture<Map<K, @NonNull V>> getAll(
      Iterable<? extends K> keys,
      BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>>> mappingFunction
   );

   void put(K key, CompletableFuture<? extends V> valueFuture);

   ConcurrentMap<K, CompletableFuture<V>> asMap();

   Cache<K, V> synchronous();
}
