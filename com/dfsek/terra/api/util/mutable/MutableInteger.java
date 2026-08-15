package com.dfsek.terra.api.util.mutable;

import org.jetbrains.annotations.NotNull;

public class MutableInteger extends MutableNumber<Integer> {
   private static final long serialVersionUID = -4427935901819632745L;

   public MutableInteger(Integer value) {
      super(value);
   }

   @Override
   public void increment() {
      this.add(1);
   }

   @Override
   public void decrement() {
      this.subtract(1);
   }

   public void add(Integer add) {
      this.value = this.value + add;
   }

   public void multiply(Integer mul) {
      this.value = this.value * mul;
   }

   public void subtract(Integer sub) {
      this.value = this.value - sub;
   }

   public void divide(Integer divide) {
      this.value = this.value / divide;
   }

   public void add(int add) {
      this.value = this.value + add;
   }

   public int compareTo(@NotNull Integer o) {
      return Integer.compare(this.value, o);
   }
}
