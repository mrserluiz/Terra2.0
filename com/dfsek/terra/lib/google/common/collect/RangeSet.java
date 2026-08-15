package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableRangeSet or TreeRangeSet")
@GwtIncompatible
public interface RangeSet<C extends Comparable> {
   boolean contains(C value);

   @Nullable Range<C> rangeContaining(C value);

   boolean intersects(Range<C> otherRange);

   boolean encloses(Range<C> otherRange);

   boolean enclosesAll(RangeSet<C> other);

   default boolean enclosesAll(Iterable<Range<C>> other) {
      for (Range<C> range : other) {
         if (!this.encloses(range)) {
            return false;
         }
      }

      return true;
   }

   boolean isEmpty();

   Range<C> span();

   Set<Range<C>> asRanges();

   Set<Range<C>> asDescendingSetOfRanges();

   RangeSet<C> complement();

   RangeSet<C> subRangeSet(Range<C> view);

   void add(Range<C> range);

   void remove(Range<C> range);

   void clear();

   void addAll(RangeSet<C> other);

   default void addAll(Iterable<Range<C>> ranges) {
      for (Range<C> range : ranges) {
         this.add(range);
      }
   }

   void removeAll(RangeSet<C> other);

   default void removeAll(Iterable<Range<C>> ranges) {
      for (Range<C> range : ranges) {
         this.remove(range);
      }
   }

   @Override
   boolean equals(@Nullable Object obj);

   @Override
   int hashCode();

   @Override
   String toString();
}
