package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
final class SequentialExecutor implements Executor {
   private static final LazyLogger log = new LazyLogger(SequentialExecutor.class);
   private final Executor executor;
   @GuardedBy("queue")
   private final Deque<Runnable> queue = new ArrayDeque<>();
   @LazyInit
   @GuardedBy("queue")
   private SequentialExecutor.WorkerRunningState workerRunningState = SequentialExecutor.WorkerRunningState.IDLE;
   @GuardedBy("queue")
   private long workerRunCount = 0L;
   @RetainedWith
   private final SequentialExecutor.QueueWorker worker = new SequentialExecutor.QueueWorker();

   SequentialExecutor(Executor executor) {
      this.executor = Preconditions.checkNotNull(executor);
   }

   @Override
   public void execute(Runnable task) {
      Preconditions.checkNotNull(task);
      Runnable submittedTask;
      long oldRunCount;
      synchronized (this.queue) {
         if (this.workerRunningState == SequentialExecutor.WorkerRunningState.RUNNING
            || this.workerRunningState == SequentialExecutor.WorkerRunningState.QUEUED) {
            this.queue.add(task);
            return;
         }

         oldRunCount = this.workerRunCount;
         submittedTask = new Runnable() {
            @Override
            public void run() {
               task.run();
            }

            @Override
            public String toString() {
               return task.toString();
            }
         };
         this.queue.add(submittedTask);
         this.workerRunningState = SequentialExecutor.WorkerRunningState.QUEUING;
      }

      try {
         this.executor.execute(this.worker);
      } catch (Throwable var12) {
         Throwable t = var12;
         synchronized (this.queue) {
            boolean removed = (
                  this.workerRunningState == SequentialExecutor.WorkerRunningState.IDLE
                     || this.workerRunningState == SequentialExecutor.WorkerRunningState.QUEUING
               )
               && this.queue.removeLastOccurrence(submittedTask);
            if (t instanceof RejectedExecutionException && !removed) {
               return;
            }

            throw t;
         }
      }

      boolean alreadyMarkedQueued = this.workerRunningState != SequentialExecutor.WorkerRunningState.QUEUING;
      if (!alreadyMarkedQueued) {
         synchronized (this.queue) {
            if (this.workerRunCount == oldRunCount && this.workerRunningState == SequentialExecutor.WorkerRunningState.QUEUING) {
               this.workerRunningState = SequentialExecutor.WorkerRunningState.QUEUED;
            }
         }
      }
   }

   @Override
   public String toString() {
      return "SequentialExecutor@" + System.identityHashCode(this) + "{" + this.executor + "}";
   }

   private final class QueueWorker implements Runnable {
      @Nullable Runnable task;

      private QueueWorker() {
      }

      @Override
      public void run() {
         try {
            this.workOnQueue();
         } catch (Error e) {
            synchronized (SequentialExecutor.this.queue) {
               SequentialExecutor.this.workerRunningState = SequentialExecutor.WorkerRunningState.IDLE;
            }

            throw e;
         }
      }

      private void workOnQueue() {
         boolean interruptedDuringTask = false;
         boolean hasSetRunning = false;

         while (true) {
            try {
               synchronized (SequentialExecutor.this.queue) {
                  if (!hasSetRunning) {
                     if (SequentialExecutor.this.workerRunningState == SequentialExecutor.WorkerRunningState.RUNNING) {
                        return;
                     }

                     SequentialExecutor.this.workerRunCount++;
                     SequentialExecutor.this.workerRunningState = SequentialExecutor.WorkerRunningState.RUNNING;
                     hasSetRunning = true;
                  }

                  this.task = SequentialExecutor.this.queue.poll();
                  if (this.task == null) {
                     SequentialExecutor.this.workerRunningState = SequentialExecutor.WorkerRunningState.IDLE;
                     return;
                  }
               }

               interruptedDuringTask |= Thread.interrupted();

               try {
                  this.task.run();
               } catch (Exception e) {
                  SequentialExecutor.log.get().log(Level.SEVERE, "Exception while executing runnable " + this.task, e);
               } finally {
                  this.task = null;
               }
            } finally {
               if (interruptedDuringTask) {
                  Thread.currentThread().interrupt();
               }
            }
         }
      }

      @Override
      public String toString() {
         Runnable currentlyRunning = this.task;
         return currentlyRunning != null
            ? "SequentialExecutorWorker{running=" + currentlyRunning + "}"
            : "SequentialExecutorWorker{state=" + SequentialExecutor.this.workerRunningState + "}";
      }
   }

   enum WorkerRunningState {
      IDLE,
      QUEUING,
      QUEUED,
      RUNNING;
   }
}
