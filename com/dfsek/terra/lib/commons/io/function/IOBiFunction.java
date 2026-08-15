package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface IOBiFunction<T, U, R> {
   default <V> IOBiFunction<T, U, V> andThen(IOFunction<? super R, ? extends V> after) {
      Objects.requireNonNull(after);
      return (t, u) -> (V)after.apply(this.apply(t, u));
   }

   R apply(T var1, U var2) throws IOException;

   default BiFunction<T, U, R> asBiFunction() {
      return (t, u) -> Uncheck.apply(this, t, u);
   }
}
