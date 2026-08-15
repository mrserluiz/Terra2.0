package com.github.benmanes.caffeine.cache;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

final class ExecutorServiceScheduler implements Scheduler, Serializable {
   private static final Logger logger = System.getLogger(ExecutorServiceScheduler.class.getName());
   private static final long serialVersionUID = 1L;
   final ScheduledExecutorService scheduledExecutorService;

   ExecutorServiceScheduler(ScheduledExecutorService scheduledExecutorService) {
      this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService);
   }

   @Override
   public Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
      Objects.requireNonNull(executor);
      Objects.requireNonNull(command);
      Objects.requireNonNull(unit);
      return this.scheduledExecutorService.isShutdown() ? DisabledFuture.instance() : this.scheduledExecutorService.schedule(() -> {
         try {
            executor.execute(command);
         } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown when submitting scheduled task", t);
            throw t;
         }
      }, delay, unit);
   }
}
