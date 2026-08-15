package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
final class TimeoutFuture<V> extends FluentFuture.TrustedFuture<V> {
   @LazyInit
   private @Nullable ListenableFuture<V> delegateRef;
   @LazyInit
   private @Nullable ScheduledFuture<?> timer;

   static <V> ListenableFuture<V> create(ListenableFuture<V> delegate, long time, TimeUnit unit, ScheduledExecutorService scheduledExecutor) {
      TimeoutFuture<V> result = new TimeoutFuture<>(delegate);
      TimeoutFuture.Fire<V> fire = new TimeoutFuture.Fire<>(result);
      result.timer = scheduledExecutor.schedule(fire, time, unit);
      delegate.addListener(fire, MoreExecutors.directExecutor());
      return result;
   }

   private TimeoutFuture(ListenableFuture<V> delegate) {
      this.delegateRef = Preconditions.checkNotNull(delegate);
   }

   @Override
   protected @Nullable String pendingToString() {
      ListenableFuture<? extends V> localInputFuture = this.delegateRef;
      ScheduledFuture<?> localTimer = this.timer;
      if (localInputFuture != null) {
         String message = "inputFuture=[" + localInputFuture + "]";
         if (localTimer != null) {
            long delay = localTimer.getDelay(TimeUnit.MILLISECONDS);
            if (delay > 0L) {
               message = message + ", remaining delay=[" + delay + " ms]";
            }
         }

         return message;
      } else {
         return null;
      }
   }

   @Override
   protected void afterDone() {
      ListenableFuture<? extends V> delegate = this.delegateRef;
      this.maybePropagateCancellationTo(delegate);
      Future<?> localTimer = this.timer;
      if (localTimer != null) {
         localTimer.cancel(false);
      }

      this.delegateRef = null;
      this.timer = null;
   }

   private static final class Fire<V> implements Runnable {
      @LazyInit
      @Nullable TimeoutFuture<V> timeoutFutureRef;

      Fire(TimeoutFuture<V> timeoutFuture) {
         this.timeoutFutureRef = timeoutFuture;
      }

      @Override
      public void run() {
         TimeoutFuture<V> timeoutFuture = this.timeoutFutureRef;
         if (timeoutFuture != null) {
            ListenableFuture<V> delegate = timeoutFuture.delegateRef;
            if (delegate != null) {
               this.timeoutFutureRef = null;
               if (delegate.isDone()) {
                  timeoutFuture.setFuture(delegate);
               } else {
                  try {
                     ScheduledFuture<?> timer = timeoutFuture.timer;
                     timeoutFuture.timer = null;
                     String message = "Timed out";

                     try {
                        if (timer != null) {
                           long overDelayMs = Math.abs(timer.getDelay(TimeUnit.MILLISECONDS));
                           if (overDelayMs > 10L) {
                              message = message + " (timeout delayed by " + overDelayMs + " ms after scheduled time)";
                           }
                        }

                        message = message + ": " + delegate;
                     } finally {
                        timeoutFuture.setException(new TimeoutFuture.TimeoutFutureException(message));
                     }
                  } finally {
                     delegate.cancel(true);
                  }
               }
            }
         }
      }
   }

   private static final class TimeoutFutureException extends TimeoutException {
      private TimeoutFutureException(String message) {
         super(message);
      }

      @Override
      public synchronized Throwable fillInStackTrace() {
         this.setStackTrace(new StackTraceElement[0]);
         return this;
      }
   }
}
