package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableDoubleSupplier<E extends Throwable> {
   double getAsDouble() throws E;
}
