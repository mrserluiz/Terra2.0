package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableLongToDoubleFunction<E extends Throwable> {
   FailableLongToDoubleFunction NOP = t -> 0.0;

   static <E extends Throwable> FailableLongToDoubleFunction<E> nop() {
      return NOP;
   }

   double applyAsDouble(long var1) throws E;
}
