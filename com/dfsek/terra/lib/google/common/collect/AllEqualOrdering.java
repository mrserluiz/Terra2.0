package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Serializable;
import java.util.List;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true)
final class AllEqualOrdering extends Ordering<Object> implements Serializable {
   static final AllEqualOrdering INSTANCE = new AllEqualOrdering();
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   @Override
   public int compare(@Nullable Object left, @Nullable Object right) {
      return 0;
   }

   @Override
   public <E> List<E> sortedCopy(Iterable<E> iterable) {
      return Lists.newArrayList(iterable);
   }

   @Override
   public <E> ImmutableList<E> immutableSortedCopy(Iterable<E> iterable) {
      return ImmutableList.copyOf(iterable);
   }

   @Override
   public <S> Ordering<S> reverse() {
      return this;
   }

   private Object readResolve() {
      return INSTANCE;
   }

   @Override
   public String toString() {
      return "Ordering.allEqual()";
   }
}
