package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Objects;

@FunctionalInterface
public interface IOTriFunction<T, U, V, R> {
   default <W> IOTriFunction<T, U, V, W> andThen(IOFunction<? super R, ? extends W> after) {
      Objects.requireNonNull(after);
      return (t, u, v) -> (W)after.apply(this.apply(t, u, v));
   }

   R apply(T var1, U var2, V var3) throws IOException;
}
