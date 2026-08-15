package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DoNotMock("Use FakeTimeLimiter")
@J2ktIncompatible
@GwtIncompatible
public interface TimeLimiter {
   <T> T newProxy(T target, Class<T> interfaceType, long timeoutDuration, TimeUnit timeoutUnit);

   default <T> T newProxy(T target, Class<T> interfaceType, Duration timeout) {
      return this.newProxy(target, interfaceType, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException, ExecutionException;

   @CanIgnoreReturnValue
   @ParametricNullness
   default <T> T callWithTimeout(Callable<T> callable, Duration timeout) throws TimeoutException, InterruptedException, ExecutionException {
      return this.callWithTimeout(callable, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   <T> T callUninterruptiblyWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, ExecutionException;

   @CanIgnoreReturnValue
   @ParametricNullness
   default <T> T callUninterruptiblyWithTimeout(Callable<T> callable, Duration timeout) throws TimeoutException, ExecutionException {
      return this.callUninterruptiblyWithTimeout(callable, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   void runWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException;

   default void runWithTimeout(Runnable runnable, Duration timeout) throws TimeoutException, InterruptedException {
      this.runWithTimeout(runnable, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   void runUninterruptiblyWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException;

   default void runUninterruptiblyWithTimeout(Runnable runnable, Duration timeout) throws TimeoutException {
      this.runUninterruptiblyWithTimeout(runnable, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }
}
