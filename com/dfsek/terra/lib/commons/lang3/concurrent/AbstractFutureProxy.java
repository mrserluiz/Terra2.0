package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractFutureProxy<V> implements Future<V> {
   private final Future<V> future;

   public AbstractFutureProxy(Future<V> future) {
      this.future = Objects.requireNonNull(future, "future");
   }

   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      return this.future.cancel(mayInterruptIfRunning);
   }

   @Override
   public V get() throws InterruptedException, ExecutionException {
      return this.future.get();
   }

   @Override
   public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return this.future.get(timeout, unit);
   }

   public Future<V> getFuture() {
      return this.future;
   }

   @Override
   public boolean isCancelled() {
      return this.future.isCancelled();
   }

   @Override
   public boolean isDone() {
      return this.future.isDone();
   }
}
