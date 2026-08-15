package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.Queues;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;

@J2ktIncompatible
@GwtIncompatible
final class ListenerCallQueue<L> {
   private static final LazyLogger logger = new LazyLogger(ListenerCallQueue.class);
   private final List<ListenerCallQueue.PerListenerQueue<L>> listeners = Collections.synchronizedList(new ArrayList<>());

   public void addListener(L listener, Executor executor) {
      Preconditions.checkNotNull(listener, "listener");
      Preconditions.checkNotNull(executor, "executor");
      this.listeners.add(new ListenerCallQueue.PerListenerQueue<>(listener, executor));
   }

   public void enqueue(ListenerCallQueue.Event<L> event) {
      this.enqueueHelper(event, event);
   }

   public void enqueue(ListenerCallQueue.Event<L> event, String label) {
      this.enqueueHelper(event, label);
   }

   private void enqueueHelper(ListenerCallQueue.Event<L> event, Object label) {
      Preconditions.checkNotNull(event, "event");
      Preconditions.checkNotNull(label, "label");
      synchronized (this.listeners) {
         for (ListenerCallQueue.PerListenerQueue<L> queue : this.listeners) {
            queue.add(event, label);
         }
      }
   }

   public void dispatch() {
      for (int i = 0; i < this.listeners.size(); i++) {
         this.listeners.get(i).dispatch();
      }
   }

   interface Event<L> {
      void call(L listener);
   }

   private static final class PerListenerQueue<L> implements Runnable {
      final L listener;
      final Executor executor;
      @GuardedBy("this")
      final Queue<ListenerCallQueue.Event<L>> waitQueue = Queues.newArrayDeque();
      @GuardedBy("this")
      final Queue<Object> labelQueue = Queues.newArrayDeque();
      @GuardedBy("this")
      boolean isThreadScheduled;

      PerListenerQueue(L listener, Executor executor) {
         this.listener = Preconditions.checkNotNull(listener);
         this.executor = Preconditions.checkNotNull(executor);
      }

      synchronized void add(ListenerCallQueue.Event<L> event, Object label) {
         this.waitQueue.add(event);
         this.labelQueue.add(label);
      }

      void dispatch() {
         boolean scheduleEventRunner = false;
         synchronized (this) {
            if (!this.isThreadScheduled) {
               this.isThreadScheduled = true;
               scheduleEventRunner = true;
            }
         }

         if (scheduleEventRunner) {
            try {
               this.executor.execute(this);
            } catch (Exception e) {
               synchronized (this) {
                  this.isThreadScheduled = false;
               }

               ListenerCallQueue.logger.get().log(Level.SEVERE, "Exception while running callbacks for " + this.listener + " on " + this.executor, e);
               throw e;
            }
         }
      }

      @Override
      public void run() {
         boolean stillRunning = true;

         try {
            while (true) {
               ListenerCallQueue.Event<L> nextToRun;
               Object nextLabel;
               synchronized (this) {
                  Preconditions.checkState(this.isThreadScheduled);
                  nextToRun = this.waitQueue.poll();
                  nextLabel = this.labelQueue.poll();
                  if (nextToRun == null) {
                     this.isThreadScheduled = false;
                     stillRunning = false;
                     return;
                  }
               }

               try {
                  nextToRun.call(this.listener);
               } catch (Exception e) {
                  ListenerCallQueue.logger.get().log(Level.SEVERE, "Exception while executing callback: " + this.listener + " " + nextLabel, e);
               }
            }
         } finally {
            if (stillRunning) {
               synchronized (this) {
                  this.isThreadScheduled = false;
               }
            }
         }
      }
   }
}
