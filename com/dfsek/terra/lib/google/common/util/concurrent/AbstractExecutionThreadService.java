package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@GwtIncompatible
@J2ktIncompatible
public abstract class AbstractExecutionThreadService implements Service {
   private final Service delegate = new AbstractService() {
      @Override
      protected final void doStart() {
         Executor executor = MoreExecutors.renamingDecorator(
            AbstractExecutionThreadService.this.executor(), () -> AbstractExecutionThreadService.this.serviceName()
         );
         executor.execute(() -> {
            try {
               AbstractExecutionThreadService.this.startUp();
               this.notifyStarted();
               if (this.isRunning()) {
                  try {
                     AbstractExecutionThreadService.this.run();
                  } catch (Throwable t) {
                     Platform.restoreInterruptIfIsInterruptedException(t);

                     try {
                        AbstractExecutionThreadService.this.shutDown();
                     } catch (Exception ignored) {
                        Platform.restoreInterruptIfIsInterruptedException(ignored);
                        t.addSuppressed(ignored);
                     }

                     this.notifyFailed(t);
                     return;
                  }
               }

               AbstractExecutionThreadService.this.shutDown();
               this.notifyStopped();
            } catch (Throwable t) {
               Platform.restoreInterruptIfIsInterruptedException(t);
               this.notifyFailed(t);
            }
         });
      }

      @Override
      protected void doStop() {
         AbstractExecutionThreadService.this.triggerShutdown();
      }

      @Override
      public String toString() {
         return AbstractExecutionThreadService.this.toString();
      }
   };

   protected AbstractExecutionThreadService() {
   }

   protected void startUp() throws Exception {
   }

   protected abstract void run() throws Exception;

   protected void shutDown() throws Exception {
   }

   protected void triggerShutdown() {
   }

   protected Executor executor() {
      return command -> MoreExecutors.newThread(this.serviceName(), command).start();
   }

   @Override
   public String toString() {
      return this.serviceName() + " [" + this.state() + "]";
   }

   @Override
   public final boolean isRunning() {
      return this.delegate.isRunning();
   }

   @Override
   public final Service.State state() {
      return this.delegate.state();
   }

   @Override
   public final void addListener(Service.Listener listener, Executor executor) {
      this.delegate.addListener(listener, executor);
   }

   @Override
   public final Throwable failureCause() {
      return this.delegate.failureCause();
   }

   @CanIgnoreReturnValue
   @Override
   public final Service startAsync() {
      this.delegate.startAsync();
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public final Service stopAsync() {
      this.delegate.stopAsync();
      return this;
   }

   @Override
   public final void awaitRunning() {
      this.delegate.awaitRunning();
   }

   @Override
   public final void awaitRunning(Duration timeout) throws TimeoutException {
      Service.super.awaitRunning(timeout);
   }

   @Override
   public final void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
      this.delegate.awaitRunning(timeout, unit);
   }

   @Override
   public final void awaitTerminated() {
      this.delegate.awaitTerminated();
   }

   @Override
   public final void awaitTerminated(Duration timeout) throws TimeoutException {
      Service.super.awaitTerminated(timeout);
   }

   @Override
   public final void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
      this.delegate.awaitTerminated(timeout, unit);
   }

   protected String serviceName() {
      return this.getClass().getSimpleName();
   }
}
