package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.util.Objects;

public class ConstantInitializer<T> implements ConcurrentInitializer<T> {
   private static final String FMT_TO_STRING = "ConstantInitializer@%d [ object = %s ]";
   private final T object;

   public ConstantInitializer(T obj) {
      this.object = obj;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof ConstantInitializer)) {
         return false;
      }

      ConstantInitializer<?> c = (ConstantInitializer<?>)obj;
      return Objects.equals(this.getObject(), c.getObject());
   }

   @Override
   public T get() throws ConcurrentException {
      return this.getObject();
   }

   public final T getObject() {
      return this.object;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.object);
   }

   public boolean isInitialized() {
      return true;
   }

   @Override
   public String toString() {
      return String.format("ConstantInitializer@%d [ object = %s ]", System.identityHashCode(this), this.getObject());
   }
}
