package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.ExecutionException;

@GwtIncompatible
public abstract class ForwardingLoadingCache<K, V> extends ForwardingCache<K, V> implements LoadingCache<K, V> {
   protected ForwardingLoadingCache() {
   }

   protected abstract LoadingCache<K, V> delegate();

   @CanIgnoreReturnValue
   @Override
   public V get(K key) throws ExecutionException {
      return this.delegate().get(key);
   }

   @CanIgnoreReturnValue
   @Override
   public V getUnchecked(K key) {
      return this.delegate().getUnchecked(key);
   }

   @CanIgnoreReturnValue
   @Override
   public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
      return this.delegate().getAll(keys);
   }

   @Override
   public V apply(K key) {
      return this.delegate().apply(key);
   }

   @Override
   public void refresh(K key) {
      this.delegate().refresh(key);
   }

   public abstract static class SimpleForwardingLoadingCache<K, V> extends ForwardingLoadingCache<K, V> {
      private final LoadingCache<K, V> delegate;

      protected SimpleForwardingLoadingCache(LoadingCache<K, V> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      protected final LoadingCache<K, V> delegate() {
         return this.delegate;
      }
   }
}
