package com.github.benmanes.caffeine.cache;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface AsyncCacheLoader<K, V> {
   CompletableFuture<? extends V> asyncLoad(K key, Executor executor) throws Exception;

   default CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>> asyncLoadAll(Set<? extends K> keys, Executor executor) throws Exception {
      throw new UnsupportedOperationException();
   }

   default CompletableFuture<? extends V> asyncReload(K key, @NonNull V oldValue, Executor executor) throws Exception {
      return this.asyncLoad(key, executor);
   }

   static <K, V> AsyncCacheLoader<K, V> bulk(Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
      return CacheLoader.bulk(mappingFunction);
   }

   static <K, V> AsyncCacheLoader<K, V> bulk(
      BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>>> mappingFunction
   ) {
      return new AsyncCacheLoader<K, V>() {
         @Override
         public CompletableFuture<V> asyncLoad(K key, Executor executor) {
            return this.asyncLoadAll(Set.of(key), executor).thenApply(results -> (V)results.get(key));
         }

         @Override
         public CompletableFuture<Map<K, V>> asyncLoadAll(Set<? extends K> keys, Executor executor) {
            Objects.requireNonNull(keys);
            Objects.requireNonNull(executor);
            return (CompletableFuture<Map<K, V>>)mappingFunction.apply(keys, executor);
         }
      };
   }
}
