package com.github.benmanes.caffeine.cache;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

final class GuardedScheduler implements Scheduler, Serializable {
   private static final Logger logger = System.getLogger(GuardedScheduler.class.getName());
   private static final long serialVersionUID = 1L;
   final Scheduler delegate;

   GuardedScheduler(Scheduler delegate) {
      this.delegate = Objects.requireNonNull(delegate);
   }

   @Override
   public Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
      try {
         Future<?> future = this.delegate.schedule(executor, command, delay, unit);
         return future == null ? DisabledFuture.instance() : future;
      } catch (Throwable t) {
         logger.log(Level.WARNING, "Exception thrown by scheduler; discarded task", t);
         return DisabledFuture.instance();
      }
   }
}
