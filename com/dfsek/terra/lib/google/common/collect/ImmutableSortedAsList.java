package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.util.Comparator;
import java.util.Spliterator;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class ImmutableSortedAsList<E> extends RegularImmutableAsList<E> implements SortedIterable<E> {
   ImmutableSortedAsList(ImmutableSortedSet<E> backingSet, ImmutableList<E> backingList) {
      super(backingSet, backingList);
   }

   ImmutableSortedSet<E> delegateCollection() {
      return (ImmutableSortedSet<E>)super.delegateCollection();
   }

   @Override
   public Comparator<? super E> comparator() {
      return this.delegateCollection().comparator();
   }

   @GwtIncompatible
   @Override
   public int indexOf(@Nullable Object target) {
      int index = this.delegateCollection().indexOf(target);
      return index >= 0 && this.get(index).equals(target) ? index : -1;
   }

   @GwtIncompatible
   @Override
   public int lastIndexOf(@Nullable Object target) {
      return this.indexOf(target);
   }

   @Override
   public boolean contains(@Nullable Object target) {
      return this.indexOf(target) >= 0;
   }

   @GwtIncompatible
   @Override
   ImmutableList<E> subListUnchecked(int fromIndex, int toIndex) {
      ImmutableList<E> parentSubList = super.subListUnchecked(fromIndex, toIndex);
      return new RegularImmutableSortedSet<>(parentSubList, this.comparator()).asList();
   }

   @Override
   public Spliterator<E> spliterator() {
      return CollectSpliterators.indexed(this.size(), 1301, this.delegateList()::get, this.comparator());
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
