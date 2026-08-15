package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
final class DescendingImmutableSortedMultiset<E> extends ImmutableSortedMultiset<E> {
   private final transient ImmutableSortedMultiset<E> forward;

   DescendingImmutableSortedMultiset(ImmutableSortedMultiset<E> forward) {
      this.forward = forward;
   }

   @Override
   public int count(@Nullable Object element) {
      return this.forward.count(element);
   }

   @Override
   public Multiset.@Nullable Entry<E> firstEntry() {
      return this.forward.lastEntry();
   }

   @Override
   public Multiset.@Nullable Entry<E> lastEntry() {
      return this.forward.firstEntry();
   }

   @Override
   public int size() {
      return this.forward.size();
   }

   @Override
   public ImmutableSortedSet<E> elementSet() {
      return this.forward.elementSet().descendingSet();
   }

   @Override
   Multiset.Entry<E> getEntry(int index) {
      return this.forward.entrySet().asList().reverse().get(index);
   }

   @Override
   public ImmutableSortedMultiset<E> descendingMultiset() {
      return this.forward;
   }

   @Override
   public ImmutableSortedMultiset<E> headMultiset(E upperBound, BoundType boundType) {
      return this.forward.tailMultiset(upperBound, boundType).descendingMultiset();
   }

   @Override
   public ImmutableSortedMultiset<E> tailMultiset(E lowerBound, BoundType boundType) {
      return this.forward.headMultiset(lowerBound, boundType).descendingMultiset();
   }

   @Override
   boolean isPartialView() {
      return this.forward.isPartialView();
   }

   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
