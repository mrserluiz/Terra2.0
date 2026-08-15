package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.function.FailableConsumer;
import com.dfsek.terra.lib.commons.lang3.function.FailableSupplier;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BackgroundInitializer<T> extends AbstractConcurrentInitializer<T, Exception> {
   private ExecutorService externalExecutor;
   private ExecutorService executor;
   private Future<T> future;

   public static <T> BackgroundInitializer.Builder<BackgroundInitializer<T>, T> builder() {
      return new BackgroundInitializer.Builder<>();
   }

   protected BackgroundInitializer() {
      this(null);
   }

   protected BackgroundInitializer(ExecutorService exec) {
      this.setExternalExecutor(exec);
   }

   private BackgroundInitializer(FailableSupplier<T, ConcurrentException> initializer, FailableConsumer<T, ConcurrentException> closer, ExecutorService exec) {
      super(initializer, closer);
      this.setExternalExecutor(exec);
   }

   private ExecutorService createExecutor() {
      return Executors.newFixedThreadPool(this.getTaskCount());
   }

   private Callable<T> createTask(ExecutorService execDestroy) {
      return new BackgroundInitializer.InitializationTask(execDestroy);
   }

   @Override
   public T get() throws ConcurrentException {
      try {
         return this.getFuture().get();
      } catch (ExecutionException execex) {
         ConcurrentUtils.handleCause(execex);
         return null;
      } catch (InterruptedException iex) {
         Thread.currentThread().interrupt();
         throw new ConcurrentException(iex);
      }
   }

   protected final synchronized ExecutorService getActiveExecutor() {
      return this.executor;
   }

   public final synchronized ExecutorService getExternalExecutor() {
      return this.externalExecutor;
   }

   public synchronized Future<T> getFuture() {
      if (this.future == null) {
         throw new IllegalStateException("start() must be called first!");
      } else {
         return this.future;
      }
   }

   protected int getTaskCount() {
      return 1;
   }

   @Override
   protected Exception getTypedException(Exception e) {
      return new Exception(e);
   }

   @Override
   public synchronized boolean isInitialized() {
      if (this.future != null && this.future.isDone()) {
         try {
            this.future.get();
            return true;
         } catch (CancellationException | ExecutionException | InterruptedException e) {
            return false;
         }
      } else {
         return false;
      }
   }

   public synchronized boolean isStarted() {
      return this.future != null;
   }

   public final synchronized void setExternalExecutor(ExecutorService externalExecutor) {
      if (this.isStarted()) {
         throw new IllegalStateException("Cannot set ExecutorService after start()!");
      }

      this.externalExecutor = externalExecutor;
   }

   public synchronized boolean start() {
      if (!this.isStarted()) {
         this.executor = this.getExternalExecutor();
         ExecutorService tempExec;
         if (this.executor == null) {
            this.executor = tempExec = this.createExecutor();
         } else {
            tempExec = null;
         }

         this.future = this.executor.submit(this.createTask(tempExec));
         return true;
      } else {
         return false;
      }
   }

   public static class Builder<I extends BackgroundInitializer<T>, T>
      extends AbstractConcurrentInitializer.AbstractBuilder<I, T, BackgroundInitializer.Builder<I, T>, Exception> {
      private ExecutorService externalExecutor;

      public I get() {
         return (I)(new BackgroundInitializer(this.getInitializer(), this.getCloser(), this.externalExecutor));
      }

      public BackgroundInitializer.Builder<I, T> setExternalExecutor(ExecutorService externalExecutor) {
         this.externalExecutor = externalExecutor;
         return this.asThis();
      }
   }

   private final class InitializationTask implements Callable<T> {
      private final ExecutorService execFinally;

      InitializationTask(ExecutorService exec) {
         this.execFinally = exec;
      }

      @Override
      public T call() throws Exception {
         try {
            return BackgroundInitializer.this.initialize();
         } finally {
            if (this.execFinally != null) {
               this.execFinally.shutdown();
            }
         }
      }
   }
}
