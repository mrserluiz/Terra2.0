package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ForwardingObject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@GwtCompatible
public abstract class ForwardingFuture<V> extends ForwardingObject implements Future<V> {
   protected ForwardingFuture() {
   }

   protected abstract Future<? extends V> delegate();

   @CanIgnoreReturnValue
   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      return this.delegate().cancel(mayInterruptIfRunning);
   }

   @Override
   public boolean isCancelled() {
      return this.delegate().isCancelled();
   }

   @Override
   public boolean isDone() {
      return this.delegate().isDone();
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public V get() throws InterruptedException, ExecutionException {
      return (V)this.delegate().get();
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return (V)this.delegate().get(timeout, unit);
   }

   public abstract static class SimpleForwardingFuture<V> extends ForwardingFuture<V> {
      private final Future<V> delegate;

      protected SimpleForwardingFuture(Future<V> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      protected final Future<V> delegate() {
         return this.delegate;
      }
   }
}
