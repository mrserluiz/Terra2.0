package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Predicate;
import java.util.List;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class FilteredKeyListMultimap<K, V> extends FilteredKeyMultimap<K, V> implements ListMultimap<K, V> {
   FilteredKeyListMultimap(ListMultimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
      super(unfiltered, keyPredicate);
   }

   public ListMultimap<K, V> unfiltered() {
      return (ListMultimap<K, V>)super.unfiltered();
   }

   @Override
   public List<V> get(@ParametricNullness K key) {
      return (List<V>)super.get(key);
   }

   @Override
   public List<V> removeAll(@Nullable Object key) {
      return (List<V>)super.removeAll(key);
   }

   @Override
   public List<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return (List<V>)super.replaceValues(key, values);
   }
}
