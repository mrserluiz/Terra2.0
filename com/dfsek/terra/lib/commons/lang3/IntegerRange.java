package com.dfsek.terra.lib.commons.lang3;

public final class IntegerRange extends NumberRange<Integer> {
   private static final long serialVersionUID = 1L;

   public static IntegerRange of(int fromInclusive, int toInclusive) {
      return of(fromInclusive, Integer.valueOf(toInclusive));
   }

   public static IntegerRange of(Integer fromInclusive, Integer toInclusive) {
      return new IntegerRange(fromInclusive, toInclusive);
   }

   private IntegerRange(Integer number1, Integer number2) {
      super(number1, number2, null);
   }
}
