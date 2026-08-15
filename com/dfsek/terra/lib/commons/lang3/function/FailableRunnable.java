package com.dfsek.terra.lib.commons.lang3.function;

@FunctionalInterface
public interface FailableRunnable<E extends Throwable> {
   void run() throws E;
}
