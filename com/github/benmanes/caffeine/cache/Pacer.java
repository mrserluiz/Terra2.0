package com.github.benmanes.caffeine.cache;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

final class Pacer {
   static final long TOLERANCE = Caffeine.ceilingPowerOfTwo(TimeUnit.SECONDS.toNanos(1L));
   final Scheduler scheduler;
   long nextFireTime;
   @Nullable Future<?> future;

   Pacer(Scheduler scheduler) {
      this.scheduler = Objects.requireNonNull(scheduler);
   }

   public void schedule(Executor executor, Runnable command, long now, long delay) {
      long scheduleAt = now + delay;
      if (this.future == null) {
         if (this.nextFireTime != 0L) {
            return;
         }
      } else if (this.nextFireTime - now > 0L) {
         if (this.maySkip(scheduleAt)) {
            return;
         }

         this.future.cancel(false);
      }

      long actualDelay = this.calculateSchedule(now, delay, scheduleAt);
      this.future = this.scheduler.schedule(executor, command, actualDelay, TimeUnit.NANOSECONDS);
   }

   public void cancel() {
      if (this.future != null) {
         this.future.cancel(false);
         this.nextFireTime = 0L;
         this.future = null;
      }
   }

   public boolean isScheduled() {
      return this.future != null && !this.future.isDone();
   }

   boolean maySkip(long scheduleAt) {
      long delta = scheduleAt - this.nextFireTime;
      return delta >= 0L || -delta <= TOLERANCE;
   }

   long calculateSchedule(long now, long delay, long scheduleAt) {
      if (delay <= TOLERANCE) {
         this.nextFireTime = now + TOLERANCE;
         return TOLERANCE;
      } else {
         this.nextFireTime = scheduleAt;
         return delay;
      }
   }
}
