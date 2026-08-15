package com.github.benmanes.caffeine.cache;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface CacheLoader<K, V> extends AsyncCacheLoader<K, V> {
   V load(K key) throws Exception;

   default Map<? extends K, ? extends @NonNull V> loadAll(Set<? extends K> keys) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   default CompletableFuture<? extends V> asyncLoad(K key, Executor executor) throws Exception {
      Objects.requireNonNull(key);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.load(key);
         } catch (RuntimeException e) {
            throw e;
         } catch (Exception e) {
            throw new CompletionException(e);
         }
      }, executor);
   }

   @Override
   default CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>> asyncLoadAll(Set<? extends K> keys, Executor executor) throws Exception {
      Objects.requireNonNull(keys);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.loadAll(keys);
         } catch (RuntimeException e) {
            throw e;
         } catch (Exception e) {
            throw new CompletionException(e);
         }
      }, executor);
   }

   default V reload(K key, @NonNull V oldValue) throws Exception {
      return this.load(key);
   }

   @Override
   default CompletableFuture<? extends V> asyncReload(K key, @NonNull V oldValue, Executor executor) throws Exception {
      Objects.requireNonNull(key);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.reload(key, oldValue);
         } catch (RuntimeException e) {
            throw e;
         } catch (Exception e) {
            throw new CompletionException(e);
         }
      }, executor);
   }

   static <K, V> CacheLoader<K, V> bulk(Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
      return new CacheLoader<K, V>() {
         @Override
         public V load(K key) {
            return (V)this.loadAll(Set.of(key)).get(key);
         }

         @Override
         public Map<? extends K, ? extends @NonNull V> loadAll(Set<? extends K> keys) {
            return (Map<? extends K, ? extends V>)mappingFunction.apply(keys);
         }
      };
   }
}
