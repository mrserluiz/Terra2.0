package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Comparator;
import java.util.NavigableSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class UnmodifiableSortedMultiset<E> extends Multisets.UnmodifiableMultiset<E> implements SortedMultiset<E> {
   @LazyInit
   private transient @Nullable UnmodifiableSortedMultiset<E> descendingMultiset;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   UnmodifiableSortedMultiset(SortedMultiset<E> delegate) {
      super(delegate);
   }

   protected SortedMultiset<E> delegate() {
      return (SortedMultiset<E>)super.delegate();
   }

   @Override
   public Comparator<? super E> comparator() {
      return this.delegate().comparator();
   }

   NavigableSet<E> createElementSet() {
      return Sets.unmodifiableNavigableSet(this.delegate().elementSet());
   }

   @Override
   public NavigableSet<E> elementSet() {
      return (NavigableSet<E>)super.elementSet();
   }

   @Override
   public SortedMultiset<E> descendingMultiset() {
      UnmodifiableSortedMultiset<E> result = this.descendingMultiset;
      if (result == null) {
         result = new UnmodifiableSortedMultiset<>(this.delegate().descendingMultiset());
         result.descendingMultiset = this;
         return this.descendingMultiset = result;
      } else {
         return result;
      }
   }

   @Override
   public Multiset.@Nullable Entry<E> firstEntry() {
      return this.delegate().firstEntry();
   }

   @Override
   public Multiset.@Nullable Entry<E> lastEntry() {
      return this.delegate().lastEntry();
   }

   @Override
   public Multiset.@Nullable Entry<E> pollFirstEntry() {
      throw new UnsupportedOperationException();
   }

   @Override
   public Multiset.@Nullable Entry<E> pollLastEntry() {
      throw new UnsupportedOperationException();
   }

   @Override
   public SortedMultiset<E> headMultiset(@ParametricNullness E upperBound, BoundType boundType) {
      return Multisets.unmodifiableSortedMultiset(this.delegate().headMultiset(upperBound, boundType));
   }

   @Override
   public SortedMultiset<E> subMultiset(@ParametricNullness E lowerBound, BoundType lowerBoundType, @ParametricNullness E upperBound, BoundType upperBoundType) {
      return Multisets.unmodifiableSortedMultiset(this.delegate().subMultiset(lowerBound, lowerBoundType, upperBound, upperBoundType));
   }

   @Override
   public SortedMultiset<E> tailMultiset(@ParametricNullness E lowerBound, BoundType boundType) {
      return Multisets.unmodifiableSortedMultiset(this.delegate().tailMultiset(lowerBound, boundType));
   }
}
