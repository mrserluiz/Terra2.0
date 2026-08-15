package com.dfsek.terra.lib.google.common.math;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.math.BigDecimal;
import java.math.RoundingMode;

@J2ktIncompatible
@GwtIncompatible
public class BigDecimalMath {
   private BigDecimalMath() {
   }

   public static double roundToDouble(BigDecimal x, RoundingMode mode) {
      return BigDecimalMath.BigDecimalToDoubleRounder.INSTANCE.roundToDouble(x, mode);
   }

   private static class BigDecimalToDoubleRounder extends ToDoubleRounder<BigDecimal> {
      static final BigDecimalMath.BigDecimalToDoubleRounder INSTANCE = new BigDecimalMath.BigDecimalToDoubleRounder();

      double roundToDoubleArbitrarily(BigDecimal bigDecimal) {
         return bigDecimal.doubleValue();
      }

      int sign(BigDecimal bigDecimal) {
         return bigDecimal.signum();
      }

      BigDecimal toX(double d, RoundingMode mode) {
         return new BigDecimal(d);
      }

      BigDecimal minus(BigDecimal a, BigDecimal b) {
         return a.subtract(b);
      }
   }
}
