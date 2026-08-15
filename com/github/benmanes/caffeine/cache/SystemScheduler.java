package com.github.benmanes.caffeine.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

enum SystemScheduler implements Scheduler {
   INSTANCE;

   @Override
   public Future<?> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
      Executor delayedExecutor = CompletableFuture.delayedExecutor(delay, unit, executor);
      return CompletableFuture.runAsync(command, delayedExecutor);
   }
}
