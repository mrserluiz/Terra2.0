package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Supplier;
import com.dfsek.terra.lib.google.common.util.concurrent.Futures;
import com.dfsek.terra.lib.google.common.util.concurrent.ListenableFuture;
import com.dfsek.terra.lib.google.common.util.concurrent.ListenableFutureTask;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Executor;

@GwtCompatible(emulated = true)
public abstract class CacheLoader<K, V> {
   protected CacheLoader() {
   }

   public abstract V load(K key) throws Exception;

   @GwtIncompatible
   public ListenableFuture<V> reload(K key, V oldValue) throws Exception {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(oldValue);
      return Futures.immediateFuture(this.load(key));
   }

   public Map<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
      throw new CacheLoader.UnsupportedLoadingOperationException();
   }

   public static <K, V> CacheLoader<K, V> from(Function<K, V> function) {
      return new CacheLoader.FunctionToCacheLoader<>(function);
   }

   public static <V> CacheLoader<Object, V> from(Supplier<V> supplier) {
      return new CacheLoader.SupplierToCacheLoader<>(supplier);
   }

   @GwtIncompatible
   public static <K, V> CacheLoader<K, V> asyncReloading(CacheLoader<K, V> loader, Executor executor) {
      Preconditions.checkNotNull(loader);
      Preconditions.checkNotNull(executor);
      return new CacheLoader<K, V>() {
         @Override
         public V load(K key) throws Exception {
            return loader.load(key);
         }

         @Override
         public ListenableFuture<V> reload(K key, V oldValue) {
            ListenableFutureTask<V> task = ListenableFutureTask.create(() -> loader.reload(key, oldValue).get());
            executor.execute(task);
            return task;
         }

         @Override
         public Map<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
            return loader.loadAll(keys);
         }
      };
   }

   private static final class FunctionToCacheLoader<K, V> extends CacheLoader<K, V> implements Serializable {
      private final Function<K, V> computingFunction;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      public FunctionToCacheLoader(Function<K, V> computingFunction) {
         this.computingFunction = Preconditions.checkNotNull(computingFunction);
      }

      @Override
      public V load(K key) {
         return this.computingFunction.apply(Preconditions.checkNotNull(key));
      }
   }

   public static final class InvalidCacheLoadException extends RuntimeException {
      public InvalidCacheLoadException(String message) {
         super(message);
      }
   }

   private static final class SupplierToCacheLoader<V> extends CacheLoader<Object, V> implements Serializable {
      private final Supplier<V> computingSupplier;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      public SupplierToCacheLoader(Supplier<V> computingSupplier) {
         this.computingSupplier = Preconditions.checkNotNull(computingSupplier);
      }

      @Override
      public V load(Object key) {
         Preconditions.checkNotNull(key);
         return this.computingSupplier.get();
      }
   }

   public static final class UnsupportedLoadingOperationException extends UnsupportedOperationException {
      UnsupportedLoadingOperationException() {
      }
   }
}
