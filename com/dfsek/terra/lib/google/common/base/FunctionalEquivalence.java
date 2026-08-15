package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class FunctionalEquivalence<F, T> extends Equivalence<F> implements Serializable {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;
   private final Function<? super F, ? extends @Nullable T> function;
   private final Equivalence<T> resultEquivalence;

   FunctionalEquivalence(Function<? super F, ? extends @Nullable T> function, Equivalence<T> resultEquivalence) {
      this.function = Preconditions.checkNotNull(function);
      this.resultEquivalence = Preconditions.checkNotNull(resultEquivalence);
   }

   @Override
   protected boolean doEquivalent(F a, F b) {
      return this.resultEquivalence.equivalent((T)this.function.apply(a), (T)this.function.apply(b));
   }

   @Override
   protected int doHash(F a) {
      return this.resultEquivalence.hash((T)this.function.apply(a));
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof FunctionalEquivalence)) {
         return false;
      }

      FunctionalEquivalence<?, ?> that = (FunctionalEquivalence<?, ?>)obj;
      return this.function.equals(that.function) && this.resultEquivalence.equals(that.resultEquivalence);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.function, this.resultEquivalence);
   }

   @Override
   public String toString() {
      return this.resultEquivalence + ".onResultOf(" + this.function + ")";
   }
}
