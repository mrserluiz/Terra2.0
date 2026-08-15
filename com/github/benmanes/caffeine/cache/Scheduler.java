package com.github.benmanes.caffeine.cache;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface Scheduler {
   Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit);

   static Scheduler disabledScheduler() {
      return DisabledScheduler.INSTANCE;
   }

   static Scheduler systemScheduler() {
      return SystemScheduler.INSTANCE;
   }

   static Scheduler forScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
      return new ExecutorServiceScheduler(scheduledExecutorService);
   }

   static Scheduler guardedScheduler(Scheduler scheduler) {
      return scheduler instanceof GuardedScheduler ? scheduler : new GuardedScheduler(scheduler);
   }
}
