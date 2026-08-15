package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@J2ktIncompatible
@GwtIncompatible
public final class FakeTimeLimiter implements TimeLimiter {
   @CanIgnoreReturnValue
   @Override
   public <T> T newProxy(T target, Class<T> interfaceType, long timeoutDuration, TimeUnit timeoutUnit) {
      Preconditions.checkNotNull(target);
      Preconditions.checkNotNull(interfaceType);
      Preconditions.checkNotNull(timeoutUnit);
      return target;
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws ExecutionException {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(timeoutUnit);

      try {
         return callable.call();
      } catch (RuntimeException e) {
         throw new UncheckedExecutionException(e);
      } catch (Exception e) {
         Platform.restoreInterruptIfIsInterruptedException(e);
         throw new ExecutionException(e);
      } catch (Error e) {
         throw new ExecutionError(e);
      }
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public <T> T callUninterruptiblyWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws ExecutionException {
      return this.callWithTimeout(callable, timeoutDuration, timeoutUnit);
   }

   @Override
   public void runWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) {
      Preconditions.checkNotNull(runnable);
      Preconditions.checkNotNull(timeoutUnit);

      try {
         runnable.run();
      } catch (Exception e) {
         throw new UncheckedExecutionException(e);
      } catch (Error e) {
         throw new ExecutionError(e);
      }
   }

   @Override
   public void runUninterruptiblyWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) {
      this.runWithTimeout(runnable, timeoutDuration, timeoutUnit);
   }
}
