package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
@J2ktIncompatible
public abstract class AbstractScheduledService implements Service {
   private static final LazyLogger logger = new LazyLogger(AbstractScheduledService.class);
   private final AbstractService delegate = new AbstractScheduledService.ServiceDelegate();

   protected AbstractScheduledService() {
   }

   protected abstract void runOneIteration() throws Exception;

   protected void startUp() throws Exception {
   }

   protected void shutDown() throws Exception {
   }

   protected abstract AbstractScheduledService.Scheduler scheduler();

   protected ScheduledExecutorService executor() {
      class ThreadFactoryImpl implements ThreadFactory {
         @Override
         public Thread newThread(Runnable runnable) {
            return MoreExecutors.newThread(AbstractScheduledService.this.serviceName(), runnable);
         }
      }

      final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl());
      this.addListener(new Service.Listener() {
         @Override
         public void terminated(Service.State from) {
            executor.shutdown();
         }

         @Override
         public void failed(Service.State from, Throwable failure) {
            executor.shutdown();
         }
      }, MoreExecutors.directExecutor());
      return executor;
   }

   protected String serviceName() {
      return this.getClass().getSimpleName();
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

   interface Cancellable {
      void cancel(boolean mayInterruptIfRunning);

      boolean isCancelled();
   }

   public abstract static class CustomScheduler extends AbstractScheduledService.Scheduler {
      @Override
      final AbstractScheduledService.Cancellable schedule(AbstractService service, ScheduledExecutorService executor, Runnable runnable) {
         return new AbstractScheduledService.CustomScheduler.ReschedulableCallable(service, executor, runnable).reschedule();
      }

      protected abstract AbstractScheduledService.CustomScheduler.Schedule getNextSchedule() throws Exception;

      private final class ReschedulableCallable implements Callable<Void> {
         private final Runnable wrappedRunnable;
         private final ScheduledExecutorService executor;
         private final AbstractService service;
         private final ReentrantLock lock = new ReentrantLock();
         @GuardedBy("lock")
         private AbstractScheduledService.CustomScheduler.@Nullable SupplantableFuture cancellationDelegate;

         ReschedulableCallable(AbstractService service, ScheduledExecutorService executor, Runnable runnable) {
            this.wrappedRunnable = runnable;
            this.executor = executor;
            this.service = service;
         }

         public @Nullable Void call() throws Exception {
            this.wrappedRunnable.run();
            this.reschedule();
            return null;
         }

         @CanIgnoreReturnValue
         public AbstractScheduledService.Cancellable reschedule() {
            AbstractScheduledService.CustomScheduler.Schedule schedule;
            try {
               schedule = CustomScheduler.this.getNextSchedule();
            } catch (Throwable t) {
               Platform.restoreInterruptIfIsInterruptedException(t);
               this.service.notifyFailed(t);
               return new AbstractScheduledService.FutureAsCancellable(Futures.immediateCancelledFuture());
            }

            Throwable scheduleFailure = null;
            this.lock.lock();

            AbstractScheduledService.Cancellable toReturn;
            try {
               toReturn = this.initializeOrUpdateCancellationDelegate(schedule);
            } catch (Throwable e) {
               scheduleFailure = e;
               toReturn = new AbstractScheduledService.FutureAsCancellable(Futures.immediateCancelledFuture());
            } finally {
               this.lock.unlock();
            }

            if (scheduleFailure != null) {
               this.service.notifyFailed(scheduleFailure);
            }

            return toReturn;
         }

         @GuardedBy("lock")
         private AbstractScheduledService.Cancellable initializeOrUpdateCancellationDelegate(AbstractScheduledService.CustomScheduler.Schedule schedule) {
            if (this.cancellationDelegate == null) {
               return this.cancellationDelegate = new AbstractScheduledService.CustomScheduler.SupplantableFuture(this.lock, this.submitToExecutor(schedule));
            }

            if (!this.cancellationDelegate.currentFuture.isCancelled()) {
               this.cancellationDelegate.currentFuture = this.submitToExecutor(schedule);
            }

            return this.cancellationDelegate;
         }

         private ScheduledFuture<@Nullable Void> submitToExecutor(AbstractScheduledService.CustomScheduler.Schedule schedule) {
            return this.executor.schedule(this, schedule.delay, schedule.unit);
         }
      }

      protected static final class Schedule {
         private final long delay;
         private final TimeUnit unit;

         public Schedule(long delay, TimeUnit unit) {
            this.delay = delay;
            this.unit = Preconditions.checkNotNull(unit);
         }

         public Schedule(Duration delay) {
            this(Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
         }
      }

      private static final class SupplantableFuture implements AbstractScheduledService.Cancellable {
         private final ReentrantLock lock;
         @GuardedBy("lock")
         private Future<@Nullable Void> currentFuture;

         SupplantableFuture(ReentrantLock lock, Future<@Nullable Void> currentFuture) {
            this.lock = lock;
            this.currentFuture = currentFuture;
         }

         @Override
         public void cancel(boolean mayInterruptIfRunning) {
            this.lock.lock();

            try {
               this.currentFuture.cancel(mayInterruptIfRunning);
            } finally {
               this.lock.unlock();
            }
         }

         @Override
         public boolean isCancelled() {
            this.lock.lock();

            try {
               return this.currentFuture.isCancelled();
            } finally {
               this.lock.unlock();
            }
         }
      }
   }

   private static final class FutureAsCancellable implements AbstractScheduledService.Cancellable {
      private final Future<?> delegate;

      FutureAsCancellable(Future<?> delegate) {
         this.delegate = delegate;
      }

      @Override
      public void cancel(boolean mayInterruptIfRunning) {
         this.delegate.cancel(mayInterruptIfRunning);
      }

      @Override
      public boolean isCancelled() {
         return this.delegate.isCancelled();
      }
   }

   public abstract static class Scheduler {
      public static AbstractScheduledService.Scheduler newFixedDelaySchedule(Duration initialDelay, Duration delay) {
         return newFixedDelaySchedule(Internal.toNanosSaturated(initialDelay), Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
      }

      public static AbstractScheduledService.Scheduler newFixedDelaySchedule(long initialDelay, long delay, TimeUnit unit) {
         Preconditions.checkNotNull(unit);
         Preconditions.checkArgument(delay > 0L, "delay must be > 0, found %s", delay);
         return new AbstractScheduledService.Scheduler() {
            @Override
            public AbstractScheduledService.Cancellable schedule(AbstractService service, ScheduledExecutorService executor, Runnable task) {
               return new AbstractScheduledService.FutureAsCancellable(executor.scheduleWithFixedDelay(task, initialDelay, delay, unit));
            }
         };
      }

      public static AbstractScheduledService.Scheduler newFixedRateSchedule(Duration initialDelay, Duration period) {
         return newFixedRateSchedule(Internal.toNanosSaturated(initialDelay), Internal.toNanosSaturated(period), TimeUnit.NANOSECONDS);
      }

      public static AbstractScheduledService.Scheduler newFixedRateSchedule(long initialDelay, long period, TimeUnit unit) {
         Preconditions.checkNotNull(unit);
         Preconditions.checkArgument(period > 0L, "period must be > 0, found %s", period);
         return new AbstractScheduledService.Scheduler() {
            @Override
            public AbstractScheduledService.Cancellable schedule(AbstractService service, ScheduledExecutorService executor, Runnable task) {
               return new AbstractScheduledService.FutureAsCancellable(executor.scheduleAtFixedRate(task, initialDelay, period, unit));
            }
         };
      }

      abstract AbstractScheduledService.Cancellable schedule(AbstractService service, ScheduledExecutorService executor, Runnable runnable);

      private Scheduler() {
      }
   }

   private final class ServiceDelegate extends AbstractService {
      private volatile AbstractScheduledService.@Nullable Cancellable runningTask;
      private volatile @Nullable ScheduledExecutorService executorService;
      private final ReentrantLock lock = new ReentrantLock();
      private final Runnable task = new AbstractScheduledService.ServiceDelegate.Task();

      private ServiceDelegate() {
      }

      @Override
      protected final void doStart() {
         this.executorService = MoreExecutors.renamingDecorator(
            AbstractScheduledService.this.executor(), () -> AbstractScheduledService.this.serviceName() + " " + this.state()
         );
         this.executorService.execute(() -> {
            this.lock.lock();

            try {
               AbstractScheduledService.this.startUp();
               Objects.requireNonNull(this.executorService);
               this.runningTask = AbstractScheduledService.this.scheduler().schedule(AbstractScheduledService.this.delegate, this.executorService, this.task);
               this.notifyStarted();
            } catch (Throwable t) {
               Platform.restoreInterruptIfIsInterruptedException(t);
               this.notifyFailed(t);
               if (this.runningTask != null) {
                  this.runningTask.cancel(false);
               }
            } finally {
               this.lock.unlock();
            }
         });
      }

      @Override
      protected final void doStop() {
         Objects.requireNonNull(this.runningTask);
         Objects.requireNonNull(this.executorService);
         this.runningTask.cancel(false);
         this.executorService.execute(() -> {
            try {
               this.lock.lock();

               label42: {
                  try {
                     if (this.state() == Service.State.STOPPING) {
                        AbstractScheduledService.this.shutDown();
                        break label42;
                     }
                  } finally {
                     this.lock.unlock();
                  }

                  return;
               }

               this.notifyStopped();
            } catch (Throwable t) {
               Platform.restoreInterruptIfIsInterruptedException(t);
               this.notifyFailed(t);
            }
         });
      }

      @Override
      public String toString() {
         return AbstractScheduledService.this.toString();
      }

      class Task implements Runnable {
         @Override
         public void run() {
            ServiceDelegate.this.lock.lock();

            try {
               if (!Objects.requireNonNull(ServiceDelegate.this.runningTask).isCancelled()) {
                  AbstractScheduledService.this.runOneIteration();
                  return;
               }
            } catch (Throwable t) {
               Platform.restoreInterruptIfIsInterruptedException(t);

               try {
                  AbstractScheduledService.this.shutDown();
               } catch (Exception ignored) {
                  Platform.restoreInterruptIfIsInterruptedException(ignored);
                  AbstractScheduledService.logger.get().log(Level.WARNING, "Error while attempting to shut down the service after failure.", ignored);
               }

               ServiceDelegate.this.notifyFailed(t);
               Objects.requireNonNull(ServiceDelegate.this.runningTask).cancel(false);
               return;
            } finally {
               ServiceDelegate.this.lock.unlock();
            }
         }
      }
   }
}
