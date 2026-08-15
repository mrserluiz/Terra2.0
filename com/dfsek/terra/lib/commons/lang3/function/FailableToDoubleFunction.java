package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableToDoubleFunction<T, E extends Throwable> {
   FailableToDoubleFunction NOP = t -> 0.0;

   static <T, E extends Throwable> FailableToDoubleFunction<T, E> nop() {
      return NOP;
   }

   double applyAsDouble(T var1) throws E;
}
