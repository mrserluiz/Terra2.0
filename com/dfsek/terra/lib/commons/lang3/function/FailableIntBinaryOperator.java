package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableIntBinaryOperator<E extends Throwable> {
   int applyAsInt(int var1, int var2) throws E;
}
