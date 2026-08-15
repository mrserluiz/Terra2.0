package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableDoubleToLongFunction<E extends Throwable> {
   FailableDoubleToLongFunction NOP = t -> 0;

   static <E extends Throwable> FailableDoubleToLongFunction<E> nop() {
      return NOP;
   }

   int applyAsLong(double var1) throws E;
}
