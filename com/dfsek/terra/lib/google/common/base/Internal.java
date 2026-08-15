package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.time.Duration;

@J2ktIncompatible
@GwtIncompatible
final class Internal {
   @IgnoreJRERequirement
   static long toNanosSaturated(Duration duration) {
      try {
         return duration.toNanos();
      } catch (ArithmeticException tooBig) {
         return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
      }
   }

   private Internal() {
   }
}
