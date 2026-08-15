package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableToDoubleBiFunction<T, U, E extends Throwable> {
   FailableToDoubleBiFunction NOP = (t, u) -> 0.0;

   static <T, U, E extends Throwable> FailableToDoubleBiFunction<T, U, E> nop() {
      return NOP;
   }

   double applyAsDouble(T var1, U var2) throws E;
}
