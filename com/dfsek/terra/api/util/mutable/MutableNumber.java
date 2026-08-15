package com.dfsek.terra.api.util.mutable;

public abstract class MutableNumber<T extends Number> extends Number implements MutablePrimitive<T> {
   private static final long serialVersionUID = 8619508342781664393L;
   protected T value;

   public MutableNumber(T value) {
      this.value = value;
   }

   public abstract void increment();

   public abstract void decrement();

   public abstract void add(T var1);

   public abstract void multiply(T var1);

   public abstract void subtract(T var1);

   public abstract void divide(T var1);

   public T get() {
      return this.value;
   }

   public void set(T value) {
      this.value = value;
   }

   @Override
   public int intValue() {
      return this.value.intValue();
   }

   @Override
   public long longValue() {
      return this.value.longValue();
   }

   @Override
   public float floatValue() {
      return this.value.floatValue();
   }

   @Override
   public double doubleValue() {
      return this.value.doubleValue();
   }
}
