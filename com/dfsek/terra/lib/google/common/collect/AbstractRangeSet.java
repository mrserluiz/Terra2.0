package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
abstract class AbstractRangeSet<C extends Comparable> implements RangeSet<C> {
   @Override
   public boolean contains(C value) {
      return this.rangeContaining(value) != null;
   }

   @Override
   public abstract @Nullable Range<C> rangeContaining(C value);

   @Override
   public boolean isEmpty() {
      return this.asRanges().isEmpty();
   }

   @Override
   public void add(Range<C> range) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(Range<C> range) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      this.remove(Range.all());
   }

   @Override
   public boolean enclosesAll(RangeSet<C> other) {
      return this.enclosesAll(other.asRanges());
   }

   @Override
   public void addAll(RangeSet<C> other) {
      this.addAll(other.asRanges());
   }

   @Override
   public void removeAll(RangeSet<C> other) {
      this.removeAll(other.asRanges());
   }

   @Override
   public boolean intersects(Range<C> otherRange) {
      return !this.subRangeSet(otherRange).isEmpty();
   }

   @Override
   public abstract boolean encloses(Range<C> otherRange);

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj == this) {
         return true;
      } else if (obj instanceof RangeSet) {
         RangeSet<?> other = (RangeSet<?>)obj;
         return this.asRanges().equals(other.asRanges());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return this.asRanges().hashCode();
   }

   @Override
   public final String toString() {
      return this.asRanges().toString();
   }
}
