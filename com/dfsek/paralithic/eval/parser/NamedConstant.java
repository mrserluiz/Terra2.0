package com.dfsek.paralithic.eval.parser;

public class NamedConstant {
   private final String name;
   private final double value;

   protected NamedConstant(String name, double value) {
      this.name = name;
      this.value = value;
   }

   public double getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      return this.name + ": " + this.value;
   }
}
