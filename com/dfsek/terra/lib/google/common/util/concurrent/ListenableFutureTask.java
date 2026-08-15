package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@J2ktIncompatible
@GwtIncompatible
public class ListenableFutureTask<V> extends FutureTask<V> implements ListenableFuture<V> {
   private final ExecutionList executionList = new ExecutionList();

   public static <V> ListenableFutureTask<V> create(Callable<V> callable) {
      return new ListenableFutureTask<>(callable);
   }

   public static <V> ListenableFutureTask<V> create(Runnable runnable, @ParametricNullness V result) {
      return new ListenableFutureTask<>(runnable, result);
   }

   ListenableFutureTask(Callable<V> callable) {
      super(callable);
   }

   ListenableFutureTask(Runnable runnable, @ParametricNullness V result) {
      super(runnable, result);
   }

   @Override
   public void addListener(Runnable listener, Executor exec) {
      this.executionList.add(listener, exec);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public V get(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException {
      long timeoutNanos = unit.toNanos(timeout);
      return timeoutNanos <= 2147483647999999999L ? super.get(timeout, unit) : super.get(Math.min(timeoutNanos, 2147483647999999999L), TimeUnit.NANOSECONDS);
   }

   @Override
   protected void done() {
      this.executionList.execute();
   }
}
