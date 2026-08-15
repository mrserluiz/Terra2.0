package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
public abstract class ForwardingNavigableSet<E> extends ForwardingSortedSet<E> implements NavigableSet<E> {
   protected ForwardingNavigableSet() {
   }

   protected abstract NavigableSet<E> delegate();

   @Override
   public @Nullable E lower(@ParametricNullness E e) {
      return this.delegate().lower(e);
   }

   protected @Nullable E standardLower(@ParametricNullness E e) {
      return Iterators.getNext(this.headSet(e, false).descendingIterator(), null);
   }

   @Override
   public @Nullable E floor(@ParametricNullness E e) {
      return this.delegate().floor(e);
   }

   protected @Nullable E standardFloor(@ParametricNullness E e) {
      return Iterators.getNext(this.headSet(e, true).descendingIterator(), null);
   }

   @Override
   public @Nullable E ceiling(@ParametricNullness E e) {
      return this.delegate().ceiling(e);
   }

   protected @Nullable E standardCeiling(@ParametricNullness E e) {
      return Iterators.getNext(this.tailSet(e, true).iterator(), null);
   }

   @Override
   public @Nullable E higher(@ParametricNullness E e) {
      return this.delegate().higher(e);
   }

   protected @Nullable E standardHigher(@ParametricNullness E e) {
      return Iterators.getNext(this.tailSet(e, false).iterator(), null);
   }

   @Override
   public @Nullable E pollFirst() {
      return this.delegate().pollFirst();
   }

   protected @Nullable E standardPollFirst() {
      return Iterators.pollNext(this.iterator());
   }

   @Override
   public @Nullable E pollLast() {
      return this.delegate().pollLast();
   }

   protected @Nullable E standardPollLast() {
      return Iterators.pollNext(this.descendingIterator());
   }

   @ParametricNullness
   protected E standardFirst() {
      return this.iterator().next();
   }

   @ParametricNullness
   protected E standardLast() {
      return this.descendingIterator().next();
   }

   @Override
   public NavigableSet<E> descendingSet() {
      return this.delegate().descendingSet();
   }

   @Override
   public Iterator<E> descendingIterator() {
      return this.delegate().descendingIterator();
   }

   @Override
   public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
      return this.delegate().subSet(fromElement, fromInclusive, toElement, toInclusive);
   }

   protected NavigableSet<E> standardSubSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
      return this.tailSet(fromElement, fromInclusive).headSet(toElement, toInclusive);
   }

   @Override
   protected SortedSet<E> standardSubSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
      return this.subSet(fromElement, true, toElement, false);
   }

   @Override
   public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
      return this.delegate().headSet(toElement, inclusive);
   }

   protected SortedSet<E> standardHeadSet(@ParametricNullness E toElement) {
      return this.headSet(toElement, false);
   }

   @Override
   public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
      return this.delegate().tailSet(fromElement, inclusive);
   }

   protected SortedSet<E> standardTailSet(@ParametricNullness E fromElement) {
      return this.tailSet(fromElement, true);
   }

   protected class StandardDescendingSet extends Sets.DescendingSet<E> {
      public StandardDescendingSet() {
         super(ForwardingNavigableSet.this);
      }
   }
}
