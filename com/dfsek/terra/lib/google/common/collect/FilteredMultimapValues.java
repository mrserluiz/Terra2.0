package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.base.Predicates;
import com.google.j2objc.annotations.Weak;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class FilteredMultimapValues<K, V> extends AbstractCollection<V> {
   @Weak
   private final FilteredMultimap<K, V> multimap;

   FilteredMultimapValues(FilteredMultimap<K, V> multimap) {
      this.multimap = Preconditions.checkNotNull(multimap);
   }

   @Override
   public Iterator<V> iterator() {
      return Maps.valueIterator(this.multimap.entries().iterator());
   }

   @Override
   public boolean contains(@Nullable Object o) {
      return this.multimap.containsValue(o);
   }

   @Override
   public int size() {
      return this.multimap.size();
   }

   @Override
   public boolean remove(@Nullable Object o) {
      Predicate<? super Entry<K, V>> entryPredicate = this.multimap.entryPredicate();
      Iterator<Entry<K, V>> unfilteredItr = this.multimap.unfiltered().entries().iterator();

      while (unfilteredItr.hasNext()) {
         Entry<K, V> entry = unfilteredItr.next();
         if (entryPredicate.apply(entry) && Objects.equal(entry.getValue(), o)) {
            unfilteredItr.remove();
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      return Iterables.removeIf(
         this.multimap.unfiltered().entries(),
         Predicates.and(this.multimap.entryPredicate(), Maps.valuePredicateOnEntries(Predicates.in((Collection<? extends V>)c)))
      );
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      return Iterables.removeIf(
         this.multimap.unfiltered().entries(),
         Predicates.and(this.multimap.entryPredicate(), Maps.valuePredicateOnEntries(Predicates.not(Predicates.in((Collection<? extends V>)c))))
      );
   }

   @Override
   public void clear() {
      this.multimap.clear();
   }
}
