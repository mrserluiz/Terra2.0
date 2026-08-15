package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.google.j2objc.annotations.ReflectionSupport;
import com.google.j2objc.annotations.ReflectionSupport.Level;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.concurrent.locks.LockSupport;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
@ReflectionSupport(Level.FULL)
abstract class InterruptibleTask<T> extends AtomicReference<Runnable> implements Runnable {
   private static final Runnable DONE = new InterruptibleTask.DoNothingRunnable();
   private static final Runnable PARKED = new InterruptibleTask.DoNothingRunnable();
   private static final int MAX_BUSY_WAIT_SPINS = 1000;

   @Override
   public final void run() {
      Thread currentThread = Thread.currentThread();
      if (this.compareAndSet(null, currentThread)) {
         boolean run = !this.isDone();
         T result = null;
         Throwable error = null;

         try {
            if (run) {
               result = this.runInterruptibly();
            }
         } catch (Throwable t) {
            Platform.restoreInterruptIfIsInterruptedException(t);
            error = t;
         } finally {
            if (!this.compareAndSet(currentThread, DONE)) {
               this.waitForInterrupt(currentThread);
            }

            if (run) {
               if (error == null) {
                  this.afterRanInterruptiblySuccess(NullnessCasts.uncheckedCastNullableTToT(result));
               } else {
                  this.afterRanInterruptiblyFailure(error);
               }
            }
         }
      }
   }

   private void waitForInterrupt(Thread currentThread) {
      boolean restoreInterruptedBit = false;
      int spinCount = 0;
      Runnable state = this.get();
      InterruptibleTask.Blocker blocker = null;

      while (state instanceof InterruptibleTask.Blocker || state == PARKED) {
         if (state instanceof InterruptibleTask.Blocker) {
            blocker = (InterruptibleTask.Blocker)state;
         }

         if (++spinCount > 1000) {
            if (state == PARKED || this.compareAndSet(state, PARKED)) {
               restoreInterruptedBit = Thread.interrupted() || restoreInterruptedBit;
               LockSupport.park(blocker);
            }
         } else {
            Thread.yield();
         }

         state = this.get();
      }

      if (restoreInterruptedBit) {
         currentThread.interrupt();
      }
   }

   abstract boolean isDone();

   @ParametricNullness
   abstract T runInterruptibly() throws Exception;

   abstract void afterRanInterruptiblySuccess(@ParametricNullness T result);

   abstract void afterRanInterruptiblyFailure(Throwable error);

   final void interruptTask() {
      Runnable currentRunner = this.get();
      if (currentRunner instanceof Thread) {
         InterruptibleTask.Blocker blocker = new InterruptibleTask.Blocker(this);
         blocker.setOwner(Thread.currentThread());
         if (this.compareAndSet(currentRunner, blocker)) {
            try {
               ((Thread)currentRunner).interrupt();
            } finally {
               Runnable prev = this.getAndSet(DONE);
               if (prev == PARKED) {
                  LockSupport.unpark((Thread)currentRunner);
               }
            }
         }
      }
   }

   @Override
   public final String toString() {
      Runnable state = this.get();
      String result;
      if (state == DONE) {
         result = "running=[DONE]";
      } else if (state instanceof InterruptibleTask.Blocker) {
         result = "running=[INTERRUPTED]";
      } else if (state instanceof Thread) {
         result = "running=[RUNNING ON " + ((Thread)state).getName() + "]";
      } else {
         result = "running=[NOT STARTED YET]";
      }

      return result + ", " + this.toPendingString();
   }

   abstract String toPendingString();

   static {
      Class<LockSupport> var0 = LockSupport.class;
   }

   @VisibleForTesting
   static final class Blocker extends AbstractOwnableSynchronizer implements Runnable {
      private final InterruptibleTask<?> task;

      private Blocker(InterruptibleTask<?> task) {
         this.task = task;
      }

      @Override
      public void run() {
      }

      private void setOwner(Thread thread) {
         super.setExclusiveOwnerThread(thread);
      }

      @VisibleForTesting
      @Nullable Thread getOwner() {
         return super.getExclusiveOwnerThread();
      }

      @Override
      public String toString() {
         return this.task.toString();
      }
   }

   private static final class DoNothingRunnable implements Runnable {
      private DoNothingRunnable() {
      }

      @Override
      public void run() {
      }
   }
}
