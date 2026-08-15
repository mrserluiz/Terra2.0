package com.dfsek.terra.api.util.mutable;

import org.jetbrains.annotations.NotNull;

public class MutableDouble extends MutableNumber<Double> {
   private static final long serialVersionUID = -2218110876763640053L;

   public MutableDouble(Double value) {
      super(value);
   }

   @Override
   public void increment() {
      this.add(1.0);
   }

   @Override
   public void decrement() {
      this.subtract(1.0);
   }

   public void add(Double add) {
      this.value = this.value + add;
   }

   public void multiply(Double mul) {
      this.value = this.value * mul;
   }

   public void subtract(Double sub) {
      this.value = this.value - sub;
   }

   public void divide(Double divide) {
      this.value = this.value / divide;
   }

   public int compareTo(@NotNull Double o) {
      return Double.compare(this.value, o);
   }
}
