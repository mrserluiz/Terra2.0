package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Predicate;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class FilteredEntrySetMultimap<K, V> extends FilteredEntryMultimap<K, V> implements FilteredSetMultimap<K, V> {
   FilteredEntrySetMultimap(SetMultimap<K, V> unfiltered, Predicate<? super Entry<K, V>> predicate) {
      super(unfiltered, predicate);
   }

   @Override
   public SetMultimap<K, V> unfiltered() {
      return (SetMultimap<K, V>)this.unfiltered;
   }

   @Override
   public Set<V> get(@ParametricNullness K key) {
      return (Set<V>)super.get(key);
   }

   @Override
   public Set<V> removeAll(@Nullable Object key) {
      return (Set<V>)super.removeAll(key);
   }

   @Override
   public Set<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return (Set<V>)super.replaceValues(key, values);
   }

   Set<Entry<K, V>> createEntries() {
      return Sets.filter(this.unfiltered().entries(), this.entryPredicate());
   }

   @Override
   public Set<Entry<K, V>> entries() {
      return (Set<Entry<K, V>>)super.entries();
   }
}
