package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface ToBooleanBiFunction<T, U> {
   boolean applyAsBoolean(T var1, U var2);
}
