package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableLongFunction<R, E extends Throwable> {
   FailableLongFunction NOP = t -> null;

   static <R, E extends Throwable> FailableLongFunction<R, E> nop() {
      return NOP;
   }

   R apply(long var1) throws E;
}
