package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@GwtIncompatible
public interface ListeningScheduledExecutorService extends ScheduledExecutorService, ListeningExecutorService {
   ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

   @J2ktIncompatible
   default ListenableScheduledFuture<?> schedule(Runnable command, Duration delay) {
      return this.schedule(command, Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
   }

   <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

   @J2ktIncompatible
   default <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, Duration delay) {
      return this.schedule(callable, Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
   }

   ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

   @J2ktIncompatible
   default ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, Duration initialDelay, Duration period) {
      return this.scheduleAtFixedRate(command, Internal.toNanosSaturated(initialDelay), Internal.toNanosSaturated(period), TimeUnit.NANOSECONDS);
   }

   ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);

   @J2ktIncompatible
   default ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, Duration initialDelay, Duration delay) {
      return this.scheduleWithFixedDelay(command, Internal.toNanosSaturated(initialDelay), Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
   }
}
