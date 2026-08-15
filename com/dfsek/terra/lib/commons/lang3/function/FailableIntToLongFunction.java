package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableIntToLongFunction<E extends Throwable> {
   FailableIntToLongFunction NOP = t -> 0L;

   static <E extends Throwable> FailableIntToLongFunction<E> nop() {
      return NOP;
   }

   long applyAsLong(int var1) throws E;
}
