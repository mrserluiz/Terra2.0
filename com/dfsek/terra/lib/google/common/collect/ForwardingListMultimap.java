package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.List;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingListMultimap<K, V> extends ForwardingMultimap<K, V> implements ListMultimap<K, V> {
   protected ForwardingListMultimap() {
   }

   protected abstract ListMultimap<K, V> delegate();

   @Override
   public List<V> get(@ParametricNullness K key) {
      return this.delegate().get(key);
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> removeAll(@Nullable Object key) {
      return this.delegate().removeAll(key);
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return this.delegate().replaceValues(key, values);
   }
}
