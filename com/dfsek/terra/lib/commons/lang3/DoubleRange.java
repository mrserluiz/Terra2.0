package com.dfsek.terra.lib.commons.lang3;

public final class DoubleRange extends NumberRange<Double> {
   private static final long serialVersionUID = 1L;

   public static DoubleRange of(double fromInclusive, double toInclusive) {
      return of(fromInclusive, Double.valueOf(toInclusive));
   }

   public static DoubleRange of(Double fromInclusive, Double toInclusive) {
      return new DoubleRange(fromInclusive, toInclusive);
   }

   private DoubleRange(Double number1, Double number2) {
      super(number1, number2, null);
   }
}
