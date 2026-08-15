package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import java.util.Comparator;
import java.util.function.ObjIntConsumer;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
final class RegularImmutableSortedMultiset<E> extends ImmutableSortedMultiset<E> {
   private static final long[] zeroCumulativeCounts = new long[]{0L};
   static final ImmutableSortedMultiset<?> NATURAL_EMPTY_MULTISET = new RegularImmutableSortedMultiset(Ordering.natural());
   @VisibleForTesting
   final transient RegularImmutableSortedSet<E> elementSet;
   private final transient long[] cumulativeCounts;
   private final transient int offset;
   private final transient int length;

   RegularImmutableSortedMultiset(Comparator<? super E> comparator) {
      this.elementSet = ImmutableSortedSet.emptySet(comparator);
      this.cumulativeCounts = zeroCumulativeCounts;
      this.offset = 0;
      this.length = 0;
   }

   RegularImmutableSortedMultiset(RegularImmutableSortedSet<E> elementSet, long[] cumulativeCounts, int offset, int length) {
      this.elementSet = elementSet;
      this.cumulativeCounts = cumulativeCounts;
      this.offset = offset;
      this.length = length;
   }

   private int getCount(int index) {
      return (int)(this.cumulativeCounts[this.offset + index + 1] - this.cumulativeCounts[this.offset + index]);
   }

   @Override
   Multiset.Entry<E> getEntry(int index) {
      return Multisets.immutableEntry((E)this.elementSet.asList().get(index), this.getCount(index));
   }

   @Override
   public void forEachEntry(ObjIntConsumer<? super E> action) {
      Preconditions.checkNotNull(action);

      for (int i = 0; i < this.length; i++) {
         action.accept((E)this.elementSet.asList().get(i), this.getCount(i));
      }
   }

   @Override
   public Multiset.@Nullable Entry<E> firstEntry() {
      return this.isEmpty() ? null : this.getEntry(0);
   }

   @Override
   public Multiset.@Nullable Entry<E> lastEntry() {
      return this.isEmpty() ? null : this.getEntry(this.length - 1);
   }

   @Override
   public int count(@Nullable Object element) {
      int index = this.elementSet.indexOf(element);
      return index >= 0 ? this.getCount(index) : 0;
   }

   @Override
   public int size() {
      long size = this.cumulativeCounts[this.offset + this.length] - this.cumulativeCounts[this.offset];
      return Ints.saturatedCast(size);
   }

   @Override
   public ImmutableSortedSet<E> elementSet() {
      return this.elementSet;
   }

   @Override
   public ImmutableSortedMultiset<E> headMultiset(E upperBound, BoundType boundType) {
      return this.getSubMultiset(0, this.elementSet.headIndex(upperBound, Preconditions.checkNotNull(boundType) == BoundType.CLOSED));
   }

   @Override
   public ImmutableSortedMultiset<E> tailMultiset(E lowerBound, BoundType boundType) {
      return this.getSubMultiset(this.elementSet.tailIndex(lowerBound, Preconditions.checkNotNull(boundType) == BoundType.CLOSED), this.length);
   }

   ImmutableSortedMultiset<E> getSubMultiset(int from, int to) {
      Preconditions.checkPositionIndexes(from, to, this.length);
      if (from == to) {
         return emptyMultiset(this.comparator());
      }

      if (from == 0 && to == this.length) {
         return this;
      }

      RegularImmutableSortedSet<E> subElementSet = this.elementSet.getSubSet(from, to);
      return new RegularImmutableSortedMultiset<>(subElementSet, this.cumulativeCounts, this.offset + from, to - from);
   }

   @Override
   boolean isPartialView() {
      return this.offset > 0 || this.length < this.cumulativeCounts.length - 1;
   }

   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
