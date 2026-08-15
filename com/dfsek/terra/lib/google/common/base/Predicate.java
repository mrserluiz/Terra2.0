package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@GwtCompatible
public interface Predicate<T> extends java.util.function.Predicate<T> {
   boolean apply(@ParametricNullness T input);

   @Override
   boolean equals(@Nullable Object object);

   @Override
   default boolean test(@ParametricNullness T input) {
      return this.apply(input);
   }
}
