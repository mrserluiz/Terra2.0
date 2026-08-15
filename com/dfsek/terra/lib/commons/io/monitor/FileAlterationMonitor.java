package com.dfsek.terra.lib.commons.io.monitor;

import com.dfsek.terra.lib.commons.io.ThreadUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

public final class FileAlterationMonitor implements Runnable {
   private static final FileAlterationObserver[] EMPTY_ARRAY = new FileAlterationObserver[0];
   private final long intervalMillis;
   private final List<FileAlterationObserver> observers = new CopyOnWriteArrayList<>();
   private Thread thread;
   private ThreadFactory threadFactory;
   private volatile boolean running;

   public FileAlterationMonitor() {
      this(10000L);
   }

   public FileAlterationMonitor(long intervalMillis) {
      this.intervalMillis = intervalMillis;
   }

   public FileAlterationMonitor(long interval, Collection<FileAlterationObserver> observers) {
      this(interval, Optional.ofNullable(observers).orElse(Collections.emptyList()).toArray(EMPTY_ARRAY));
   }

   public FileAlterationMonitor(long interval, FileAlterationObserver... observers) {
      this(interval);
      if (observers != null) {
         Stream.of(observers).forEach(this::addObserver);
      }
   }

   public void addObserver(FileAlterationObserver observer) {
      if (observer != null) {
         this.observers.add(observer);
      }
   }

   public long getInterval() {
      return this.intervalMillis;
   }

   public Iterable<FileAlterationObserver> getObservers() {
      return new ArrayList<>(this.observers);
   }

   public void removeObserver(FileAlterationObserver observer) {
      if (observer != null) {
         this.observers.removeIf(observer::equals);
      }
   }

   @Override
   public void run() {
      while (this.running) {
         this.observers.forEach(FileAlterationObserver::checkAndNotify);
         if (this.running) {
            try {
               ThreadUtils.sleep(Duration.ofMillis(this.intervalMillis));
            } catch (InterruptedException var2) {
            }
            continue;
         }
         break;
      }
   }

   public synchronized void setThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
   }

   public synchronized void start() throws Exception {
      if (this.running) {
         throw new IllegalStateException("Monitor is already running");
      }

      for (FileAlterationObserver observer : this.observers) {
         observer.initialize();
      }

      this.running = true;
      if (this.threadFactory != null) {
         this.thread = this.threadFactory.newThread(this);
      } else {
         this.thread = new Thread(this);
      }

      this.thread.start();
   }

   public synchronized void stop() throws Exception {
      this.stop(this.intervalMillis);
   }

   public synchronized void stop(long stopInterval) throws Exception {
      if (!this.running) {
         throw new IllegalStateException("Monitor is not running");
      }

      this.running = false;

      try {
         this.thread.interrupt();
         this.thread.join(stopInterval);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }

      for (FileAlterationObserver observer : this.observers) {
         observer.destroy();
      }
   }
}
