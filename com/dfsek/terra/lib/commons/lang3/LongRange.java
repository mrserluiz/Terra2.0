package com.dfsek.terra.lib.commons.lang3;

public final class LongRange extends NumberRange<Long> {
   private static final long serialVersionUID = 1L;

   public static LongRange of(long fromInclusive, long toInclusive) {
      return of(fromInclusive, Long.valueOf(toInclusive));
   }

   public static LongRange of(Long fromInclusive, Long toInclusive) {
      return new LongRange(fromInclusive, toInclusive);
   }

   private LongRange(Long number1, Long number2) {
      super(number1, number2, null);
   }
}
