package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.InlineMe;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@Immutable(containerOf = "C")
@GwtCompatible
public final class Range<C extends Comparable> extends RangeGwtSerializationDependencies implements Predicate<C>, Serializable {
   private static final Range<Comparable> ALL = new Range<>(Cut.belowAll(), Cut.aboveAll());
   final Cut<C> lowerBound;
   final Cut<C> upperBound;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   static <C extends Comparable<?>> Ordering<Range<C>> rangeLexOrdering() {
      return (Ordering<Range<C>>)Range.RangeLexOrdering.INSTANCE;
   }

   static <C extends Comparable<?>> Range<C> create(Cut<C> lowerBound, Cut<C> upperBound) {
      return new Range<>(lowerBound, upperBound);
   }

   public static <C extends Comparable<?>> Range<C> open(C lower, C upper) {
      return create(Cut.aboveValue(lower), Cut.belowValue(upper));
   }

   public static <C extends Comparable<?>> Range<C> closed(C lower, C upper) {
      return create(Cut.belowValue(lower), Cut.aboveValue(upper));
   }

   public static <C extends Comparable<?>> Range<C> closedOpen(C lower, C upper) {
      return create(Cut.belowValue(lower), Cut.belowValue(upper));
   }

   public static <C extends Comparable<?>> Range<C> openClosed(C lower, C upper) {
      return create(Cut.aboveValue(lower), Cut.aboveValue(upper));
   }

   public static <C extends Comparable<?>> Range<C> range(C lower, BoundType lowerType, C upper, BoundType upperType) {
      Preconditions.checkNotNull(lowerType);
      Preconditions.checkNotNull(upperType);
      Cut<C> lowerBound = lowerType == BoundType.OPEN ? Cut.aboveValue(lower) : Cut.belowValue(lower);
      Cut<C> upperBound = upperType == BoundType.OPEN ? Cut.belowValue(upper) : Cut.aboveValue(upper);
      return create(lowerBound, upperBound);
   }

   public static <C extends Comparable<?>> Range<C> lessThan(C endpoint) {
      return create(Cut.belowAll(), Cut.belowValue(endpoint));
   }

   public static <C extends Comparable<?>> Range<C> atMost(C endpoint) {
      return create(Cut.belowAll(), Cut.aboveValue(endpoint));
   }

   public static <C extends Comparable<?>> Range<C> upTo(C endpoint, BoundType boundType) {
      switch (boundType) {
         case OPEN:
            return lessThan(endpoint);
         case CLOSED:
            return atMost(endpoint);
         default:
            throw new AssertionError();
      }
   }

   public static <C extends Comparable<?>> Range<C> greaterThan(C endpoint) {
      return create(Cut.aboveValue(endpoint), Cut.aboveAll());
   }

   public static <C extends Comparable<?>> Range<C> atLeast(C endpoint) {
      return create(Cut.belowValue(endpoint), Cut.aboveAll());
   }

   public static <C extends Comparable<?>> Range<C> downTo(C endpoint, BoundType boundType) {
      switch (boundType) {
         case OPEN:
            return greaterThan(endpoint);
         case CLOSED:
            return atLeast(endpoint);
         default:
            throw new AssertionError();
      }
   }

   public static <C extends Comparable<?>> Range<C> all() {
      return (Range<C>)ALL;
   }

   public static <C extends Comparable<?>> Range<C> singleton(C value) {
      return closed(value, value);
   }

   public static <C extends Comparable<?>> Range<C> encloseAll(Iterable<C> values) {
      Preconditions.checkNotNull(values);
      if (values instanceof SortedSet) {
         SortedSet<C> set = (SortedSet<C>)values;
         Comparator<?> comparator = set.comparator();
         if (Ordering.natural().equals(comparator) || comparator == null) {
            return closed(set.first(), set.last());
         }
      }

      Iterator<C> valueIterator = values.iterator();
      C min = (C)Preconditions.checkNotNull(valueIterator.next());
      C max = min;

      while (valueIterator.hasNext()) {
         C value = (C)Preconditions.checkNotNull(valueIterator.next());
         min = (C)Ordering.natural().min(min, value);
         max = (C)Ordering.natural().max(max, value);
      }

      return closed(min, max);
   }

   private Range(Cut<C> lowerBound, Cut<C> upperBound) {
      this.lowerBound = Preconditions.checkNotNull(lowerBound);
      this.upperBound = Preconditions.checkNotNull(upperBound);
      if (lowerBound.compareTo(upperBound) > 0 || lowerBound == Cut.aboveAll() || upperBound == Cut.belowAll()) {
         throw new IllegalArgumentException("Invalid range: " + toString(lowerBound, upperBound));
      }
   }

   public boolean hasLowerBound() {
      return this.lowerBound != Cut.belowAll();
   }

   public C lowerEndpoint() {
      return this.lowerBound.endpoint();
   }

   public BoundType lowerBoundType() {
      return this.lowerBound.typeAsLowerBound();
   }

   public boolean hasUpperBound() {
      return this.upperBound != Cut.aboveAll();
   }

   public C upperEndpoint() {
      return this.upperBound.endpoint();
   }

   public BoundType upperBoundType() {
      return this.upperBound.typeAsUpperBound();
   }

   public boolean isEmpty() {
      return this.lowerBound.equals(this.upperBound);
   }

   public boolean contains(C value) {
      Preconditions.checkNotNull(value);
      return this.lowerBound.isLessThan(value) && !this.upperBound.isLessThan(value);
   }

   @Deprecated
   @InlineMe(replacement = "this.contains(input)")
   public boolean apply(C input) {
      return this.contains(input);
   }

   @Deprecated
   @InlineMe(replacement = "this.contains(input)")
   public boolean test(C input) {
      return this.contains(input);
   }

   public boolean containsAll(Iterable<? extends C> values) {
      if (Iterables.isEmpty(values)) {
         return true;
      }

      if (values instanceof SortedSet) {
         SortedSet<? extends C> set = (SortedSet<? extends C>)values;
         Comparator<?> comparator = set.comparator();
         if (Ordering.natural().equals(comparator) || comparator == null) {
            return this.contains((C)set.first()) && this.contains((C)set.last());
         }
      }

      for (C value : values) {
         if (!this.contains(value)) {
            return false;
         }
      }

      return true;
   }

   public boolean encloses(Range<C> other) {
      return this.lowerBound.compareTo(other.lowerBound) <= 0 && this.upperBound.compareTo(other.upperBound) >= 0;
   }

   public boolean isConnected(Range<C> other) {
      return this.lowerBound.compareTo(other.upperBound) <= 0 && other.lowerBound.compareTo(this.upperBound) <= 0;
   }

   public Range<C> intersection(Range<C> connectedRange) {
      int lowerCmp = this.lowerBound.compareTo(connectedRange.lowerBound);
      int upperCmp = this.upperBound.compareTo(connectedRange.upperBound);
      if (lowerCmp >= 0 && upperCmp <= 0) {
         return this;
      }

      if (lowerCmp <= 0 && upperCmp >= 0) {
         return connectedRange;
      }

      Cut<C> newLower = lowerCmp >= 0 ? this.lowerBound : connectedRange.lowerBound;
      Cut<C> newUpper = upperCmp <= 0 ? this.upperBound : connectedRange.upperBound;
      Preconditions.checkArgument(newLower.compareTo(newUpper) <= 0, "intersection is undefined for disconnected ranges %s and %s", this, connectedRange);
      return create(newLower, newUpper);
   }

   public Range<C> gap(Range<C> otherRange) {
      if (this.lowerBound.compareTo(otherRange.upperBound) < 0 && otherRange.lowerBound.compareTo(this.upperBound) < 0) {
         throw new IllegalArgumentException("Ranges have a nonempty intersection: " + this + ", " + otherRange);
      }

      boolean isThisFirst = this.lowerBound.compareTo(otherRange.lowerBound) < 0;
      Range<C> firstRange = isThisFirst ? this : otherRange;
      Range<C> secondRange = isThisFirst ? otherRange : this;
      return create(firstRange.upperBound, secondRange.lowerBound);
   }

   public Range<C> span(Range<C> other) {
      int lowerCmp = this.lowerBound.compareTo(other.lowerBound);
      int upperCmp = this.upperBound.compareTo(other.upperBound);
      if (lowerCmp <= 0 && upperCmp >= 0) {
         return this;
      }

      if (lowerCmp >= 0 && upperCmp <= 0) {
         return other;
      }

      Cut<C> newLower = lowerCmp <= 0 ? this.lowerBound : other.lowerBound;
      Cut<C> newUpper = upperCmp >= 0 ? this.upperBound : other.upperBound;
      return create(newLower, newUpper);
   }

   public Range<C> canonical(DiscreteDomain<C> domain) {
      Preconditions.checkNotNull(domain);
      Cut<C> lower = this.lowerBound.canonical(domain);
      Cut<C> upper = this.upperBound.canonical(domain);
      return lower == this.lowerBound && upper == this.upperBound ? this : create(lower, upper);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (!(object instanceof Range)) {
         return false;
      }

      Range<?> other = (Range<?>)object;
      return this.lowerBound.equals(other.lowerBound) && this.upperBound.equals(other.upperBound);
   }

   @Override
   public int hashCode() {
      return this.lowerBound.hashCode() * 31 + this.upperBound.hashCode();
   }

   @Override
   public String toString() {
      return toString(this.lowerBound, this.upperBound);
   }

   private static String toString(Cut<?> lowerBound, Cut<?> upperBound) {
      StringBuilder sb = new StringBuilder(16);
      lowerBound.describeAsLowerBound(sb);
      sb.append("..");
      upperBound.describeAsUpperBound(sb);
      return sb.toString();
   }

   Cut<C> lowerBound() {
      return this.lowerBound;
   }

   Cut<C> upperBound() {
      return this.upperBound;
   }

   Object readResolve() {
      return this.equals(ALL) ? all() : this;
   }

   static int compareOrThrow(Comparable left, Comparable right) {
      return left.compareTo(right);
   }

   private static class RangeLexOrdering extends Ordering<Range<?>> implements Serializable {
      static final Ordering<?> INSTANCE = new Range.RangeLexOrdering();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      public int compare(Range<?> left, Range<?> right) {
         return ComparisonChain.start().compare(left.lowerBound, right.lowerBound).compare(left.upperBound, right.upperBound).result();
      }
   }
}
