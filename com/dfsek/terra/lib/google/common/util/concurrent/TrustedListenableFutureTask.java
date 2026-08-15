package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import org.jspecify.annotations.Nullable;

@GwtCompatible
class TrustedListenableFutureTask<V> extends FluentFuture.TrustedFuture<V> implements RunnableFuture<V> {
   private volatile @Nullable InterruptibleTask<?> task;

   static <V> TrustedListenableFutureTask<V> create(AsyncCallable<V> callable) {
      return new TrustedListenableFutureTask<>(callable);
   }

   static <V> TrustedListenableFutureTask<V> create(Callable<V> callable) {
      return new TrustedListenableFutureTask<>(callable);
   }

   static <V> TrustedListenableFutureTask<V> create(Runnable runnable, @ParametricNullness V result) {
      return new TrustedListenableFutureTask<>(Executors.callable(runnable, result));
   }

   TrustedListenableFutureTask(Callable<V> callable) {
      this.task = new TrustedListenableFutureTask.TrustedFutureInterruptibleTask(callable);
   }

   TrustedListenableFutureTask(AsyncCallable<V> callable) {
      this.task = new TrustedListenableFutureTask.TrustedFutureInterruptibleAsyncTask(callable);
   }

   @Override
   public void run() {
      InterruptibleTask<?> localTask = this.task;
      if (localTask != null) {
         localTask.run();
      }

      this.task = null;
   }

   @Override
   protected void afterDone() {
      super.afterDone();
      if (this.wasInterrupted()) {
         InterruptibleTask<?> localTask = this.task;
         if (localTask != null) {
            localTask.interruptTask();
         }
      }

      this.task = null;
   }

   @Override
   protected @Nullable String pendingToString() {
      InterruptibleTask<?> localTask = this.task;
      return localTask != null ? "task=[" + localTask + "]" : super.pendingToString();
   }

   private final class TrustedFutureInterruptibleAsyncTask extends InterruptibleTask<ListenableFuture<V>> {
      private final AsyncCallable<V> callable;

      TrustedFutureInterruptibleAsyncTask(AsyncCallable<V> callable) {
         this.callable = Preconditions.checkNotNull(callable);
      }

      @Override
      final boolean isDone() {
         return TrustedListenableFutureTask.this.isDone();
      }

      ListenableFuture<V> runInterruptibly() throws Exception {
         return Preconditions.checkNotNull(
            this.callable.call(), "AsyncCallable.call returned null instead of a Future. Did you mean to return immediateFuture(null)? %s", this.callable
         );
      }

      void afterRanInterruptiblySuccess(ListenableFuture<V> result) {
         TrustedListenableFutureTask.this.setFuture(result);
      }

      @Override
      void afterRanInterruptiblyFailure(Throwable error) {
         TrustedListenableFutureTask.this.setException(error);
      }

      @Override
      String toPendingString() {
         return this.callable.toString();
      }
   }

   private final class TrustedFutureInterruptibleTask extends InterruptibleTask<V> {
      private final Callable<V> callable;

      TrustedFutureInterruptibleTask(Callable<V> callable) {
         this.callable = Preconditions.checkNotNull(callable);
      }

      @Override
      final boolean isDone() {
         return TrustedListenableFutureTask.this.isDone();
      }

      @ParametricNullness
      @Override
      V runInterruptibly() throws Exception {
         return this.callable.call();
      }

      @Override
      void afterRanInterruptiblySuccess(@ParametricNullness V result) {
         TrustedListenableFutureTask.this.set(result);
      }

      @Override
      void afterRanInterruptiblyFailure(Throwable error) {
         TrustedListenableFutureTask.this.setException(error);
      }

      @Override
      String toPendingString() {
         return this.callable.toString();
      }
   }
}
