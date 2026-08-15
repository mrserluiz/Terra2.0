package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingSortedSetMultimap<K, V> extends ForwardingSetMultimap<K, V> implements SortedSetMultimap<K, V> {
   protected ForwardingSortedSetMultimap() {
   }

   protected abstract SortedSetMultimap<K, V> delegate();

   @Override
   public SortedSet<V> get(@ParametricNullness K key) {
      return this.delegate().get(key);
   }

   @Override
   public SortedSet<V> removeAll(@Nullable Object key) {
      return this.delegate().removeAll(key);
   }

   @Override
   public SortedSet<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return this.delegate().replaceValues(key, values);
   }

   @Override
   public @Nullable Comparator<? super V> valueComparator() {
      return this.delegate().valueComparator();
   }
}
