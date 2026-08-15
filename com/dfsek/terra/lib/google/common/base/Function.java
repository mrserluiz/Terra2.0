package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@GwtCompatible
public interface Function<F, T> extends java.util.function.Function<F, T> {
   @ParametricNullness
   @Override
   T apply(@ParametricNullness F input);

   @Override
   boolean equals(@Nullable Object object);
}
