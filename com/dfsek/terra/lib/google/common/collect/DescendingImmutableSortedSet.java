package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
final class DescendingImmutableSortedSet<E> extends ImmutableSortedSet<E> {
   private final ImmutableSortedSet<E> forward;

   DescendingImmutableSortedSet(ImmutableSortedSet<E> forward) {
      super(Ordering.from(forward.comparator()).reverse());
      this.forward = forward;
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return this.forward.contains(object);
   }

   @Override
   public int size() {
      return this.forward.size();
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      return this.forward.descendingIterator();
   }

   @Override
   ImmutableSortedSet<E> headSetImpl(E toElement, boolean inclusive) {
      return this.forward.tailSet(toElement, inclusive).descendingSet();
   }

   @Override
   ImmutableSortedSet<E> subSetImpl(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      return this.forward.subSet(toElement, toInclusive, fromElement, fromInclusive).descendingSet();
   }

   @Override
   ImmutableSortedSet<E> tailSetImpl(E fromElement, boolean inclusive) {
      return this.forward.headSet(fromElement, inclusive).descendingSet();
   }

   @GwtIncompatible("NavigableSet")
   @Override
   public ImmutableSortedSet<E> descendingSet() {
      return this.forward;
   }

   @GwtIncompatible("NavigableSet")
   @Override
   public UnmodifiableIterator<E> descendingIterator() {
      return this.forward.iterator();
   }

   @GwtIncompatible("NavigableSet")
   @Override
   ImmutableSortedSet<E> createDescendingSet() {
      throw new AssertionError("should never be called");
   }

   @Override
   public @Nullable E lower(E element) {
      return this.forward.higher(element);
   }

   @Override
   public @Nullable E floor(E element) {
      return this.forward.ceiling(element);
   }

   @Override
   public @Nullable E ceiling(E element) {
      return this.forward.floor(element);
   }

   @Override
   public @Nullable E higher(E element) {
      return this.forward.lower(element);
   }

   @Override
   int indexOf(@Nullable Object target) {
      int index = this.forward.indexOf(target);
      return index == -1 ? index : this.size() - 1 - index;
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
