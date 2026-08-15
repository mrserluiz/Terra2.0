package com.dfsek.terra.api.util.function;

@FunctionalInterface
public interface IntIntObjConsumer<T> {
   void accept(int var1, int var2, T var3);
}
