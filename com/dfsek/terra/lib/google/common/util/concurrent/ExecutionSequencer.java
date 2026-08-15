package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
public final class ExecutionSequencer {
   private final AtomicReference<ListenableFuture<@Nullable Void>> ref = new AtomicReference<>(Futures.immediateVoidFuture());
   @LazyInit
   private ExecutionSequencer.ThreadConfinedTaskQueue latestTaskQueue = new ExecutionSequencer.ThreadConfinedTaskQueue();

   private ExecutionSequencer() {
   }

   public static ExecutionSequencer create() {
      return new ExecutionSequencer();
   }

   public <T> ListenableFuture<T> submit(Callable<T> callable, Executor executor) {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(executor);
      return this.submitAsync(new AsyncCallable<T>() {
         @Override
         public ListenableFuture<T> call() throws Exception {
            return Futures.immediateFuture(callable.call());
         }

         @Override
         public String toString() {
            return callable.toString();
         }
      }, executor);
   }

   public <T> ListenableFuture<T> submitAsync(AsyncCallable<T> callable, Executor executor) {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(executor);
      final ExecutionSequencer.TaskNonReentrantExecutor taskExecutor = new ExecutionSequencer.TaskNonReentrantExecutor(executor, this);
      AsyncCallable<T> task = new AsyncCallable<T>() {
         @Override
         public ListenableFuture<T> call() throws Exception {
            return !taskExecutor.trySetStarted() ? Futures.immediateCancelledFuture() : callable.call();
         }

         @Override
         public String toString() {
            return callable.toString();
         }
      };
      SettableFuture<Void> newFuture = SettableFuture.create();
      ListenableFuture<Void> oldFuture = this.ref.getAndSet(newFuture);
      TrustedListenableFutureTask<T> taskFuture = TrustedListenableFutureTask.create(task);
      oldFuture.addListener(taskFuture, taskExecutor);
      ListenableFuture<T> outputFuture = Futures.nonCancellationPropagating(taskFuture);
      Runnable listener = () -> {
         if (taskFuture.isDone()) {
            newFuture.setFuture(oldFuture);
         } else if (outputFuture.isCancelled() && taskExecutor.trySetCancelled()) {
            taskFuture.cancel(false);
         }
      };
      outputFuture.addListener(listener, MoreExecutors.directExecutor());
      taskFuture.addListener(listener, MoreExecutors.directExecutor());
      return outputFuture;
   }

   enum RunningState {
      NOT_RUN,
      CANCELLED,
      STARTED;
   }

   private static final class TaskNonReentrantExecutor extends AtomicReference<ExecutionSequencer.RunningState> implements Executor, Runnable {
      @Nullable ExecutionSequencer sequencer;
      @Nullable Executor delegate;
      @Nullable Runnable task;
      @LazyInit
      @Nullable Thread submitting;

      private TaskNonReentrantExecutor(Executor delegate, ExecutionSequencer sequencer) {
         super(ExecutionSequencer.RunningState.NOT_RUN);
         this.delegate = delegate;
         this.sequencer = sequencer;
      }

      @Override
      public void execute(Runnable task) {
         if (this.get() == ExecutionSequencer.RunningState.CANCELLED) {
            this.delegate = null;
            this.sequencer = null;
         } else {
            this.submitting = Thread.currentThread();

            try {
               ExecutionSequencer.ThreadConfinedTaskQueue submittingTaskQueue = Objects.requireNonNull(this.sequencer).latestTaskQueue;
               if (submittingTaskQueue.thread == this.submitting) {
                  this.sequencer = null;
                  Preconditions.checkState(submittingTaskQueue.nextTask == null);
                  submittingTaskQueue.nextTask = task;
                  submittingTaskQueue.nextExecutor = Objects.requireNonNull(this.delegate);
                  this.delegate = null;
               } else {
                  Executor localDelegate = Objects.requireNonNull(this.delegate);
                  this.delegate = null;
                  this.task = task;
                  localDelegate.execute(this);
               }
            } finally {
               this.submitting = null;
            }
         }
      }

      @Override
      public void run() {
         Thread currentThread = Thread.currentThread();
         if (currentThread != this.submitting) {
            Runnable localTask = Objects.requireNonNull(this.task);
            this.task = null;
            localTask.run();
         } else {
            ExecutionSequencer.ThreadConfinedTaskQueue executingTaskQueue = new ExecutionSequencer.ThreadConfinedTaskQueue();
            executingTaskQueue.thread = currentThread;
            Objects.requireNonNull(this.sequencer).latestTaskQueue = executingTaskQueue;
            this.sequencer = null;

            try {
               Runnable localTask = Objects.requireNonNull(this.task);
               this.task = null;
               localTask.run();

               while (true) {
                  Runnable queuedTask = executingTaskQueue.nextTask;
                  if (executingTaskQueue.nextTask == null) {
                     break;
                  }

                  Executor queuedExecutor = executingTaskQueue.nextExecutor;
                  if (executingTaskQueue.nextExecutor == null) {
                     break;
                  }

                  executingTaskQueue.nextTask = null;
                  executingTaskQueue.nextExecutor = null;
                  queuedExecutor.execute(queuedTask);
               }
            } finally {
               executingTaskQueue.thread = null;
            }
         }
      }

      private boolean trySetStarted() {
         return this.compareAndSet(ExecutionSequencer.RunningState.NOT_RUN, ExecutionSequencer.RunningState.STARTED);
      }

      private boolean trySetCancelled() {
         return this.compareAndSet(ExecutionSequencer.RunningState.NOT_RUN, ExecutionSequencer.RunningState.CANCELLED);
      }
   }

   private static final class ThreadConfinedTaskQueue {
      @LazyInit
      @Nullable Thread thread;
      @Nullable Runnable nextTask;
      @Nullable Executor nextExecutor;

      private ThreadConfinedTaskQueue() {
      }
   }
}
