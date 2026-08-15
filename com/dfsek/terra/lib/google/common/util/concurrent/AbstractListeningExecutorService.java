package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

@CheckReturnValue
@GwtIncompatible
public abstract class AbstractListeningExecutorService extends AbstractExecutorService implements ListeningExecutorService {
   @CanIgnoreReturnValue
   @Override
   protected final <T> RunnableFuture<T> newTaskFor(Runnable runnable, @ParametricNullness T value) {
      return TrustedListenableFutureTask.create(runnable, value);
   }

   @CanIgnoreReturnValue
   @Override
   protected final <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
      return TrustedListenableFutureTask.create(callable);
   }

   @CanIgnoreReturnValue
   @Override
   public ListenableFuture<?> submit(Runnable task) {
      return (ListenableFuture<?>)super.submit(task);
   }

   @CanIgnoreReturnValue
   @Override
   public <T> ListenableFuture<T> submit(Runnable task, @ParametricNullness T result) {
      return (ListenableFuture<T>)super.<T>submit(task, result);
   }

   @CanIgnoreReturnValue
   @Override
   public <T> ListenableFuture<T> submit(Callable<T> task) {
      return (ListenableFuture<T>)super.<T>submit(task);
   }
}
