package com.dfsek.terra.lib.commons.lang3.concurrent;

public interface CircuitBreaker<T> {
   boolean checkState();

   void close();

   boolean incrementAndCheckState(T var1);

   boolean isClosed();

   boolean isOpen();

   void open();
}
