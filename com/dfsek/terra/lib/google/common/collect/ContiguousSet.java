package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.DoNotCall;
import java.util.NoSuchElementException;
import java.util.Objects;

@GwtCompatible(emulated = true)
public abstract class ContiguousSet<C extends Comparable> extends ImmutableSortedSet<C> {
   final DiscreteDomain<C> domain;

   public static <C extends Comparable> ContiguousSet<C> create(Range<C> range, DiscreteDomain<C> domain) {
      Preconditions.checkNotNull(range);
      Preconditions.checkNotNull(domain);
      Range<C> effectiveRange = range;

      try {
         if (!range.hasLowerBound()) {
            effectiveRange = effectiveRange.intersection(Range.atLeast(domain.minValue()));
         }

         if (!range.hasUpperBound()) {
            effectiveRange = effectiveRange.intersection(Range.atMost(domain.maxValue()));
         }
      } catch (NoSuchElementException e) {
         throw new IllegalArgumentException(e);
      }

      boolean empty;
      if (effectiveRange.isEmpty()) {
         empty = true;
      } else {
         C afterLower = (C)Objects.requireNonNull(range.lowerBound.leastValueAbove(domain));
         C beforeUpper = (C)Objects.requireNonNull(range.upperBound.greatestValueBelow(domain));
         empty = Range.compareOrThrow(afterLower, beforeUpper) > 0;
      }

      return empty ? new EmptyContiguousSet<>(domain) : new RegularContiguousSet<>(effectiveRange, domain);
   }

   public static ContiguousSet<Integer> closed(int lower, int upper) {
      return create(Range.closed(lower, upper), DiscreteDomain.integers());
   }

   public static ContiguousSet<Long> closed(long lower, long upper) {
      return create(Range.closed(lower, upper), DiscreteDomain.longs());
   }

   public static ContiguousSet<Integer> closedOpen(int lower, int upper) {
      return create(Range.closedOpen(lower, upper), DiscreteDomain.integers());
   }

   public static ContiguousSet<Long> closedOpen(long lower, long upper) {
      return create(Range.closedOpen(lower, upper), DiscreteDomain.longs());
   }

   ContiguousSet(DiscreteDomain<C> domain) {
      super(Ordering.natural());
      this.domain = domain;
   }

   public ContiguousSet<C> headSet(C toElement) {
      return this.headSetImpl(Preconditions.checkNotNull(toElement), false);
   }

   @GwtIncompatible
   public ContiguousSet<C> headSet(C toElement, boolean inclusive) {
      return this.headSetImpl(Preconditions.checkNotNull(toElement), inclusive);
   }

   public ContiguousSet<C> subSet(C fromElement, C toElement) {
      Preconditions.checkNotNull(fromElement);
      Preconditions.checkNotNull(toElement);
      Preconditions.checkArgument(this.comparator().compare(fromElement, toElement) <= 0);
      return this.subSetImpl(fromElement, true, toElement, false);
   }

   @GwtIncompatible
   public ContiguousSet<C> subSet(C fromElement, boolean fromInclusive, C toElement, boolean toInclusive) {
      Preconditions.checkNotNull(fromElement);
      Preconditions.checkNotNull(toElement);
      Preconditions.checkArgument(this.comparator().compare(fromElement, toElement) <= 0);
      return this.subSetImpl(fromElement, fromInclusive, toElement, toInclusive);
   }

   public ContiguousSet<C> tailSet(C fromElement) {
      return this.tailSetImpl(Preconditions.checkNotNull(fromElement), true);
   }

   @GwtIncompatible
   public ContiguousSet<C> tailSet(C fromElement, boolean inclusive) {
      return this.tailSetImpl(Preconditions.checkNotNull(fromElement), inclusive);
   }

   abstract ContiguousSet<C> headSetImpl(C toElement, boolean inclusive);

   abstract ContiguousSet<C> subSetImpl(C fromElement, boolean fromInclusive, C toElement, boolean toInclusive);

   abstract ContiguousSet<C> tailSetImpl(C fromElement, boolean inclusive);

   public abstract ContiguousSet<C> intersection(ContiguousSet<C> other);

   public abstract Range<C> range();

   public abstract Range<C> range(BoundType lowerBoundType, BoundType upperBoundType);

   @GwtIncompatible
   @Override
   ImmutableSortedSet<C> createDescendingSet() {
      return new DescendingImmutableSortedSet<>(this);
   }

   @Override
   public String toString() {
      return this.range().toString();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   public static <E> ImmutableSortedSet.Builder<E> builder() {
      throw new UnsupportedOperationException();
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
