package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Serializable;
import java.util.List;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true)
final class ExplicitOrdering<T> extends Ordering<T> implements Serializable {
   final ImmutableMap<T, Integer> rankMap;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   ExplicitOrdering(List<T> valuesInOrder) {
      this(Maps.indexMap(valuesInOrder));
   }

   ExplicitOrdering(ImmutableMap<T, Integer> rankMap) {
      this.rankMap = rankMap;
   }

   @Override
   public int compare(T left, T right) {
      return this.rank(left) - this.rank(right);
   }

   private int rank(T value) {
      Integer rank = this.rankMap.get(value);
      if (rank == null) {
         throw new Ordering.IncomparableValueException(value);
      } else {
         return rank;
      }
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object instanceof ExplicitOrdering) {
         ExplicitOrdering<?> that = (ExplicitOrdering<?>)object;
         return this.rankMap.equals(that.rankMap);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.rankMap.hashCode();
   }

   @Override
   public String toString() {
      return "Ordering.explicit(" + this.rankMap.keySet() + ")";
   }
}
