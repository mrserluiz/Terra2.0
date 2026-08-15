package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableDoubleBinaryOperator<E extends Throwable> {
   double applyAsDouble(double var1, double var3) throws E;
}
