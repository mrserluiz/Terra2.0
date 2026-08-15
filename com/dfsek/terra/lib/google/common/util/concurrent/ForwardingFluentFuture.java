package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@GwtCompatible
final class ForwardingFluentFuture<V> extends FluentFuture<V> {
   private final ListenableFuture<V> delegate;

   ForwardingFluentFuture(ListenableFuture<V> delegate) {
      this.delegate = Preconditions.checkNotNull(delegate);
   }

   @Override
   public void addListener(Runnable listener, Executor executor) {
      this.delegate.addListener(listener, executor);
   }

   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      return this.delegate.cancel(mayInterruptIfRunning);
   }

   @Override
   public boolean isCancelled() {
      return this.delegate.isCancelled();
   }

   @Override
   public boolean isDone() {
      return this.delegate.isDone();
   }

   @ParametricNullness
   @Override
   public V get() throws InterruptedException, ExecutionException {
      return this.delegate.get();
   }

   @ParametricNullness
   @Override
   public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return this.delegate.get(timeout, unit);
   }

   @Override
   public String toString() {
      return this.delegate.toString();
   }
}
