package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableSupplier<T, E extends Throwable> {
   FailableSupplier NUL = () -> null;

   static <T, E extends Exception> FailableSupplier<T, E> nul() {
      return NUL;
   }

   T get() throws E;
}
