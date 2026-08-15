package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public abstract class ForwardingSortedMultiset<E> extends ForwardingMultiset<E> implements SortedMultiset<E> {
   protected ForwardingSortedMultiset() {
   }

   protected abstract SortedMultiset<E> delegate();

   @Override
   public NavigableSet<E> elementSet() {
      return this.delegate().elementSet();
   }

   @Override
   public Comparator<? super E> comparator() {
      return this.delegate().comparator();
   }

   @Override
   public SortedMultiset<E> descendingMultiset() {
      return this.delegate().descendingMultiset();
   }

   @Override
   public Multiset.@Nullable Entry<E> firstEntry() {
      return this.delegate().firstEntry();
   }

   protected Multiset.@Nullable Entry<E> standardFirstEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      }

      Multiset.Entry<E> entry = entryIterator.next();
      return Multisets.immutableEntry(entry.getElement(), entry.getCount());
   }

   @Override
   public Multiset.@Nullable Entry<E> lastEntry() {
      return this.delegate().lastEntry();
   }

   protected Multiset.@Nullable Entry<E> standardLastEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.descendingMultiset().entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      }

      Multiset.Entry<E> entry = entryIterator.next();
      return Multisets.immutableEntry(entry.getElement(), entry.getCount());
   }

   @Override
   public Multiset.@Nullable Entry<E> pollFirstEntry() {
      return this.delegate().pollFirstEntry();
   }

   protected Multiset.@Nullable Entry<E> standardPollFirstEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      }

      Multiset.Entry<E> entry = entryIterator.next();
      entry = Multisets.immutableEntry(entry.getElement(), entry.getCount());
      entryIterator.remove();
      return entry;
   }

   @Override
   public Multiset.@Nullable Entry<E> pollLastEntry() {
      return this.delegate().pollLastEntry();
   }

   protected Multiset.@Nullable Entry<E> standardPollLastEntry() {
      Iterator<Multiset.Entry<E>> entryIterator = this.descendingMultiset().entrySet().iterator();
      if (!entryIterator.hasNext()) {
         return null;
      }

      Multiset.Entry<E> entry = entryIterator.next();
      entry = Multisets.immutableEntry(entry.getElement(), entry.getCount());
      entryIterator.remove();
      return entry;
   }

   @Override
   public SortedMultiset<E> headMultiset(@ParametricNullness E upperBound, BoundType boundType) {
      return this.delegate().headMultiset(upperBound, boundType);
   }

   @Override
   public SortedMultiset<E> subMultiset(@ParametricNullness E lowerBound, BoundType lowerBoundType, @ParametricNullness E upperBound, BoundType upperBoundType) {
      return this.delegate().subMultiset(lowerBound, lowerBoundType, upperBound, upperBoundType);
   }

   protected SortedMultiset<E> standardSubMultiset(
      @ParametricNullness E lowerBound, BoundType lowerBoundType, @ParametricNullness E upperBound, BoundType upperBoundType
   ) {
      return this.tailMultiset(lowerBound, lowerBoundType).headMultiset(upperBound, upperBoundType);
   }

   @Override
   public SortedMultiset<E> tailMultiset(@ParametricNullness E lowerBound, BoundType boundType) {
      return this.delegate().tailMultiset(lowerBound, boundType);
   }

   protected abstract class StandardDescendingMultiset extends DescendingMultiset<E> {
      public StandardDescendingMultiset() {
      }

      @Override
      SortedMultiset<E> forwardMultiset() {
         return ForwardingSortedMultiset.this;
      }
   }

   protected class StandardElementSet extends SortedMultisets.NavigableElementSet<E> {
      public StandardElementSet() {
         super(ForwardingSortedMultiset.this);
      }
   }
}
