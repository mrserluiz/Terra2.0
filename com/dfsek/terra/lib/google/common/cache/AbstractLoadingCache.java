package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.Maps;
import com.dfsek.terra.lib.google.common.util.concurrent.UncheckedExecutionException;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@GwtIncompatible
public abstract class AbstractLoadingCache<K, V> extends AbstractCache<K, V> implements LoadingCache<K, V> {
   protected AbstractLoadingCache() {
   }

   @CanIgnoreReturnValue
   @Override
   public V getUnchecked(K key) {
      try {
         return this.get(key);
      } catch (ExecutionException e) {
         throw new UncheckedExecutionException(e.getCause());
      }
   }

   @Override
   public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
      Map<K, V> result = Maps.newLinkedHashMap();

      for (K key : keys) {
         if (!result.containsKey(key)) {
            result.put(key, this.get(key));
         }
      }

      return ImmutableMap.copyOf(result);
   }

   @Override
   public final V apply(K key) {
      return this.getUnchecked(key);
   }

   @Override
   public void refresh(K key) {
      throw new UnsupportedOperationException();
   }
}
