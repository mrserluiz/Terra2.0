package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DoNotMock(
   "Use TestingExecutors.sameThreadScheduledExecutor, or wrap a real Executor from java.util.concurrent.Executors with MoreExecutors.listeningDecorator"
)
@GwtIncompatible
public interface ListeningExecutorService extends ExecutorService {
   <T> ListenableFuture<T> submit(Callable<T> task);

   ListenableFuture<?> submit(Runnable task);

   <T> ListenableFuture<T> submit(Runnable task, @ParametricNullness T result);

   @Override
   <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;

   @Override
   <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException;

   @J2ktIncompatible
   default <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, Duration timeout) throws InterruptedException {
      return this.invokeAll(tasks, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   @J2ktIncompatible
   default <T> T invokeAny(Collection<? extends Callable<T>> tasks, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
      return this.invokeAny(tasks, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   @J2ktIncompatible
   default boolean awaitTermination(Duration timeout) throws InterruptedException {
      return this.awaitTermination(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }
}
