package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.collect.ForwardingObject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@J2ktIncompatible
@GwtIncompatible
public abstract class ForwardingExecutorService extends ForwardingObject implements ExecutorService {
   protected ForwardingExecutorService() {
   }

   protected abstract ExecutorService delegate();

   @CheckReturnValue
   @Override
   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().awaitTermination(timeout, unit);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return this.delegate().invokeAll(tasks);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().invokeAll(tasks, timeout, unit);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return this.delegate().invokeAny(tasks);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return this.delegate().invokeAny(tasks, timeout, unit);
   }

   @Override
   public boolean isShutdown() {
      return this.delegate().isShutdown();
   }

   @Override
   public boolean isTerminated() {
      return this.delegate().isTerminated();
   }

   @Override
   public void shutdown() {
      this.delegate().shutdown();
   }

   @CanIgnoreReturnValue
   @Override
   public List<Runnable> shutdownNow() {
      return this.delegate().shutdownNow();
   }

   @Override
   public void execute(Runnable command) {
      this.delegate().execute(command);
   }

   @Override
   public <T> Future<T> submit(Callable<T> task) {
      return this.delegate().submit(task);
   }

   @Override
   public Future<?> submit(Runnable task) {
      return this.delegate().submit(task);
   }

   @Override
   public <T> Future<T> submit(Runnable task, @ParametricNullness T result) {
      return this.delegate().submit(task, result);
   }
}
