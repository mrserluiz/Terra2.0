package com.dfsek.terra.lib.commons.io;

import java.time.Duration;
import java.time.Instant;

public final class ThreadUtils {
   private static int getNanosOfMilli(Duration duration) {
      return duration.getNano() % 1000000;
   }

   public static void sleep(Duration duration) throws InterruptedException {
      try {
         long nanoStart = System.nanoTime();
         long finishNanos = nanoStart + duration.toNanos();
         Duration remainingDuration = duration;

         long nowNano;
         do {
            Thread.sleep(remainingDuration.toMillis(), getNanosOfMilli(remainingDuration));
            nowNano = System.nanoTime();
            remainingDuration = Duration.ofNanos(finishNanos - nowNano);
         } while (nowNano - finishNanos < 0L);
      } catch (ArithmeticException e) {
         Instant finishInstant = Instant.now().plus(duration);
         Duration remainingDuration = duration;

         do {
            Thread.sleep(remainingDuration.toMillis(), getNanosOfMilli(remainingDuration));
            remainingDuration = Duration.between(Instant.now(), finishInstant);
         } while (!remainingDuration.isNegative());
      }
   }
}
