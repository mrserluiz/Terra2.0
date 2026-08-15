package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true)
final class ByFunctionOrdering<F, T> extends Ordering<F> implements Serializable {
   final Function<F, ? extends T> function;
   final Ordering<T> ordering;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   ByFunctionOrdering(Function<F, ? extends T> function, Ordering<T> ordering) {
      this.function = Preconditions.checkNotNull(function);
      this.ordering = Preconditions.checkNotNull(ordering);
   }

   @Override
   public int compare(@ParametricNullness F left, @ParametricNullness F right) {
      return this.ordering.compare((T)this.function.apply(left), (T)this.function.apply(right));
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      }

      if (!(object instanceof ByFunctionOrdering)) {
         return false;
      }

      ByFunctionOrdering<?, ?> that = (ByFunctionOrdering<?, ?>)object;
      return this.function.equals(that.function) && this.ordering.equals(that.ordering);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.function, this.ordering);
   }

   @Override
   public String toString() {
      return this.ordering + ".onResultOf(" + this.function + ")";
   }
}
