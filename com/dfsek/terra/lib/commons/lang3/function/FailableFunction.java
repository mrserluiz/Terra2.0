package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface FailableFunction<T, R, E extends Throwable> {
   FailableFunction NOP = t -> null;

   static <T, R, E extends Throwable> FailableFunction<T, R, E> function(FailableFunction<T, R, E> function) {
      return function;
   }

   static <T, E extends Throwable> FailableFunction<T, T, E> identity() {
      return t -> t;
   }

   static <T, R, E extends Throwable> FailableFunction<T, R, E> nop() {
      return NOP;
   }

   default <V> FailableFunction<T, V, E> andThen(FailableFunction<? super R, ? extends V, E> after) {
      Objects.requireNonNull(after);
      return t -> (V)after.apply(this.apply(t));
   }

   R apply(T var1) throws E;

   default <V> FailableFunction<V, R, E> compose(FailableFunction<? super V, ? extends T, E> before) {
      Objects.requireNonNull(before);
      return v -> this.apply((T)before.apply(v));
   }
}
