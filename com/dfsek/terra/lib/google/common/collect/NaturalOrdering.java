package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true)
final class NaturalOrdering extends Ordering<Comparable<?>> implements Serializable {
   static final NaturalOrdering INSTANCE = new NaturalOrdering();
   @LazyInit
   private transient @Nullable Ordering<@Nullable Comparable<?>> nullsFirst;
   @LazyInit
   private transient @Nullable Ordering<@Nullable Comparable<?>> nullsLast;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public int compare(Comparable<?> left, Comparable<?> right) {
      Preconditions.checkNotNull(left);
      Preconditions.checkNotNull(right);
      return ((Comparable<Comparable<?>>)left).compareTo(right);
   }

   @Override
   public <S extends Comparable<?>> Ordering<S> nullsFirst() {
      Ordering<Comparable<?>> result = this.nullsFirst;
      if (result == null) {
         result = this.nullsFirst = super.nullsFirst();
      }

      return result;
   }

   @Override
   public <S extends Comparable<?>> Ordering<S> nullsLast() {
      Ordering<Comparable<?>> result = this.nullsLast;
      if (result == null) {
         result = this.nullsLast = super.nullsLast();
      }

      return result;
   }

   @Override
   public <S extends Comparable<?>> Ordering<S> reverse() {
      return ReverseNaturalOrdering.INSTANCE;
   }

   private Object readResolve() {
      return INSTANCE;
   }

   @Override
   public String toString() {
      return "Ordering.natural()";
   }

   private NaturalOrdering() {
   }
}
