package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true)
final class CompoundOrdering<T> extends Ordering<T> implements Serializable {
   final Comparator<? super T>[] comparators;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   CompoundOrdering(Comparator<? super T> primary, Comparator<? super T> secondary) {
      this.comparators = new Comparator[]{primary, secondary};
   }

   CompoundOrdering(Iterable<? extends Comparator<? super T>> comparators) {
      this.comparators = Iterables.toArray(comparators, new Comparator[0]);
   }

   @Override
   public int compare(@ParametricNullness T left, @ParametricNullness T right) {
      for (int i = 0; i < this.comparators.length; i++) {
         int result = this.comparators[i].compare(left, right);
         if (result != 0) {
            return result;
         }
      }

      return 0;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      } else if (object instanceof CompoundOrdering) {
         CompoundOrdering<?> that = (CompoundOrdering<?>)object;
         return Arrays.equals(this.comparators, that.comparators);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.comparators);
   }

   @Override
   public String toString() {
      return "Ordering.compound(" + Arrays.toString(this.comparators) + ")";
   }
}
