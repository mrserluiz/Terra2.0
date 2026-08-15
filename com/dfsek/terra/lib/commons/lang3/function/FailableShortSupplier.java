package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableShortSupplier<E extends Throwable> {
   short getAsShort() throws E;
}
