package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.math.LongMath;
import java.util.concurrent.TimeUnit;

@J2ktIncompatible
@GwtIncompatible
abstract class SmoothRateLimiter extends RateLimiter {
   double storedPermits;
   double maxPermits;
   double stableIntervalMicros;
   private long nextFreeTicketMicros = 0L;

   private SmoothRateLimiter(RateLimiter.SleepingStopwatch stopwatch) {
      super(stopwatch);
   }

   @Override
   final void doSetRate(double permitsPerSecond, long nowMicros) {
      this.resync(nowMicros);
      double stableIntervalMicros = TimeUnit.SECONDS.toMicros(1L) / permitsPerSecond;
      this.stableIntervalMicros = stableIntervalMicros;
      this.doSetRate(permitsPerSecond, stableIntervalMicros);
   }

   abstract void doSetRate(double permitsPerSecond, double stableIntervalMicros);

   @Override
   final double doGetRate() {
      return TimeUnit.SECONDS.toMicros(1L) / this.stableIntervalMicros;
   }

   @Override
   final long queryEarliestAvailable(long nowMicros) {
      return this.nextFreeTicketMicros;
   }

   @Override
   final long reserveEarliestAvailable(int requiredPermits, long nowMicros) {
      this.resync(nowMicros);
      long returnValue = this.nextFreeTicketMicros;
      double storedPermitsToSpend = Math.min(requiredPermits, this.storedPermits);
      double freshPermits = requiredPermits - storedPermitsToSpend;
      long waitMicros = this.storedPermitsToWaitTime(this.storedPermits, storedPermitsToSpend) + (long)(freshPermits * this.stableIntervalMicros);
      this.nextFreeTicketMicros = LongMath.saturatedAdd(this.nextFreeTicketMicros, waitMicros);
      this.storedPermits -= storedPermitsToSpend;
      return returnValue;
   }

   abstract long storedPermitsToWaitTime(double storedPermits, double permitsToTake);

   abstract double coolDownIntervalMicros();

   void resync(long nowMicros) {
      if (nowMicros > this.nextFreeTicketMicros) {
         double newPermits = (nowMicros - this.nextFreeTicketMicros) / this.coolDownIntervalMicros();
         this.storedPermits = Math.min(this.maxPermits, this.storedPermits + newPermits);
         this.nextFreeTicketMicros = nowMicros;
      }
   }

   static final class SmoothBursty extends SmoothRateLimiter {
      final double maxBurstSeconds;

      SmoothBursty(RateLimiter.SleepingStopwatch stopwatch, double maxBurstSeconds) {
         super(stopwatch);
         this.maxBurstSeconds = maxBurstSeconds;
      }

      @Override
      void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
         double oldMaxPermits = this.maxPermits;
         this.maxPermits = this.maxBurstSeconds * permitsPerSecond;
         if (oldMaxPermits == Double.POSITIVE_INFINITY) {
            this.storedPermits = this.maxPermits;
         } else {
            this.storedPermits = oldMaxPermits == 0.0 ? 0.0 : this.storedPermits * this.maxPermits / oldMaxPermits;
         }
      }

      @Override
      long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
         return 0L;
      }

      @Override
      double coolDownIntervalMicros() {
         return this.stableIntervalMicros;
      }
   }

   static final class SmoothWarmingUp extends SmoothRateLimiter {
      private final long warmupPeriodMicros;
      private double slope;
      private double thresholdPermits;
      private double coldFactor;

      SmoothWarmingUp(RateLimiter.SleepingStopwatch stopwatch, long warmupPeriod, TimeUnit timeUnit, double coldFactor) {
         super(stopwatch);
         this.warmupPeriodMicros = timeUnit.toMicros(warmupPeriod);
         this.coldFactor = coldFactor;
      }

      @Override
      void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
         double oldMaxPermits = this.maxPermits;
         double coldIntervalMicros = stableIntervalMicros * this.coldFactor;
         this.thresholdPermits = 0.5 * this.warmupPeriodMicros / stableIntervalMicros;
         this.maxPermits = this.thresholdPermits + 2.0 * this.warmupPeriodMicros / (stableIntervalMicros + coldIntervalMicros);
         this.slope = (coldIntervalMicros - stableIntervalMicros) / (this.maxPermits - this.thresholdPermits);
         if (oldMaxPermits == Double.POSITIVE_INFINITY) {
            this.storedPermits = 0.0;
         } else {
            this.storedPermits = oldMaxPermits == 0.0 ? this.maxPermits : this.storedPermits * this.maxPermits / oldMaxPermits;
         }
      }

      @Override
      long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
         double availablePermitsAboveThreshold = storedPermits - this.thresholdPermits;
         long micros = 0L;
         if (availablePermitsAboveThreshold > 0.0) {
            double permitsAboveThresholdToTake = Math.min(availablePermitsAboveThreshold, permitsToTake);
            double length = this.permitsToTime(availablePermitsAboveThreshold)
               + this.permitsToTime(availablePermitsAboveThreshold - permitsAboveThresholdToTake);
            micros = (long)(permitsAboveThresholdToTake * length / 2.0);
            permitsToTake -= permitsAboveThresholdToTake;
         }

         return micros + (long)(this.stableIntervalMicros * permitsToTake);
      }

      private double permitsToTime(double permits) {
         return this.stableIntervalMicros + permits * this.slope;
      }

      @Override
      double coolDownIntervalMicros() {
         return this.warmupPeriodMicros / this.maxPermits;
      }
   }
}
