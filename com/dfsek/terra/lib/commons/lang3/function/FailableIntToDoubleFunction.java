package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableIntToDoubleFunction<E extends Throwable> {
   FailableIntToDoubleFunction NOP = t -> 0.0;

   static <E extends Throwable> FailableIntToDoubleFunction<E> nop() {
      return NOP;
   }

   double applyAsDouble(int var1) throws E;
}
