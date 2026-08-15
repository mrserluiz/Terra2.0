package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class ExecutionList {
   private static final LazyLogger log = new LazyLogger(ExecutionList.class);
   @GuardedBy("this")
   private ExecutionList.@Nullable RunnableExecutorPair runnables;
   @GuardedBy("this")
   private boolean executed;

   public void add(Runnable runnable, Executor executor) {
      Preconditions.checkNotNull(runnable, "Runnable was null.");
      Preconditions.checkNotNull(executor, "Executor was null.");
      synchronized (this) {
         if (!this.executed) {
            this.runnables = new ExecutionList.RunnableExecutorPair(runnable, executor, this.runnables);
            return;
         }
      }

      executeListener(runnable, executor);
   }

   public void execute() {
      ExecutionList.RunnableExecutorPair list;
      synchronized (this) {
         if (this.executed) {
            return;
         }

         this.executed = true;
         list = this.runnables;
         this.runnables = null;
      }

      ExecutionList.RunnableExecutorPair reversedList = null;

      while (list != null) {
         ExecutionList.RunnableExecutorPair tmp = list;
         list = list.next;
         tmp.next = reversedList;
         reversedList = tmp;
      }

      while (reversedList != null) {
         executeListener(reversedList.runnable, reversedList.executor);
         reversedList = reversedList.next;
      }
   }

   private static void executeListener(Runnable runnable, Executor executor) {
      try {
         executor.execute(runnable);
      } catch (Exception e) {
         log.get().log(Level.SEVERE, "RuntimeException while executing runnable " + runnable + " with executor " + executor, e);
      }
   }

   private static final class RunnableExecutorPair {
      final Runnable runnable;
      final Executor executor;
      ExecutionList.@Nullable RunnableExecutorPair next;

      RunnableExecutorPair(Runnable runnable, Executor executor, ExecutionList.@Nullable RunnableExecutorPair next) {
         this.runnable = runnable;
         this.executor = executor;
         this.next = next;
      }
   }
}
