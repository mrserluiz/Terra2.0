package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableCollection;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class CombinedFuture<V> extends AggregateFuture<Object, V> {
   @LazyInit
   private CombinedFuture.@Nullable CombinedFutureInterruptibleTask<?> task;

   CombinedFuture(ImmutableCollection<? extends ListenableFuture<?>> futures, boolean allMustSucceed, Executor listenerExecutor, AsyncCallable<V> callable) {
      super(futures, allMustSucceed, false);
      this.task = new CombinedFuture.AsyncCallableInterruptibleTask(callable, listenerExecutor);
      this.init();
   }

   CombinedFuture(ImmutableCollection<? extends ListenableFuture<?>> futures, boolean allMustSucceed, Executor listenerExecutor, Callable<V> callable) {
      super(futures, allMustSucceed, false);
      this.task = new CombinedFuture.CallableInterruptibleTask(callable, listenerExecutor);
      this.init();
   }

   @Override
   void collectOneValue(int index, @Nullable Object returnValue) {
   }

   @Override
   void handleAllCompleted() {
      CombinedFuture<V>.CombinedFutureInterruptibleTask<?> localTask = this.task;
      if (localTask != null) {
         localTask.execute();
      }
   }

   @Override
   void releaseResources(AggregateFuture.ReleaseResourcesReason reason) {
      super.releaseResources(reason);
      if (reason == AggregateFuture.ReleaseResourcesReason.OUTPUT_FUTURE_DONE) {
         this.task = null;
      }
   }

   @Override
   protected void interruptTask() {
      CombinedFuture<V>.CombinedFutureInterruptibleTask<?> localTask = this.task;
      if (localTask != null) {
         localTask.interruptTask();
      }
   }

   private final class AsyncCallableInterruptibleTask extends CombinedFuture<V>.CombinedFutureInterruptibleTask<ListenableFuture<V>> {
      private final AsyncCallable<V> callable;

      AsyncCallableInterruptibleTask(AsyncCallable<V> callable, Executor listenerExecutor) {
         super(listenerExecutor);
         this.callable = Preconditions.checkNotNull(callable);
      }

      ListenableFuture<V> runInterruptibly() throws Exception {
         ListenableFuture<V> result = this.callable.call();
         return Preconditions.checkNotNull(
            result, "AsyncCallable.call returned null instead of a Future. Did you mean to return immediateFuture(null)? %s", this.callable
         );
      }

      void setValue(ListenableFuture<V> value) {
         CombinedFuture.this.setFuture(value);
      }

      @Override
      String toPendingString() {
         return this.callable.toString();
      }
   }

   private final class CallableInterruptibleTask extends CombinedFuture<V>.CombinedFutureInterruptibleTask<V> {
      private final Callable<V> callable;

      CallableInterruptibleTask(Callable<V> callable, Executor listenerExecutor) {
         super(listenerExecutor);
         this.callable = Preconditions.checkNotNull(callable);
      }

      @ParametricNullness
      @Override
      V runInterruptibly() throws Exception {
         return this.callable.call();
      }

      @Override
      void setValue(@ParametricNullness V value) {
         CombinedFuture.this.set(value);
      }

      @Override
      String toPendingString() {
         return this.callable.toString();
      }
   }

   private abstract class CombinedFutureInterruptibleTask<T> extends InterruptibleTask<T> {
      private final Executor listenerExecutor;

      CombinedFutureInterruptibleTask(Executor listenerExecutor) {
         this.listenerExecutor = Preconditions.checkNotNull(listenerExecutor);
      }

      @Override
      final boolean isDone() {
         return CombinedFuture.this.isDone();
      }

      final void execute() {
         try {
            this.listenerExecutor.execute(this);
         } catch (RejectedExecutionException e) {
            CombinedFuture.this.setException(e);
         }
      }

      @Override
      final void afterRanInterruptiblySuccess(@ParametricNullness T result) {
         CombinedFuture.this.task = null;
         this.setValue(result);
      }

      @Override
      final void afterRanInterruptiblyFailure(Throwable error) {
         CombinedFuture.this.task = null;
         if (error instanceof ExecutionException) {
            CombinedFuture.this.setException(((ExecutionException)error).getCause());
         } else if (error instanceof CancellationException) {
            CombinedFuture.this.cancel(false);
         } else {
            CombinedFuture.this.setException(error);
         }
      }

      abstract void setValue(@ParametricNullness T value);
   }
}
