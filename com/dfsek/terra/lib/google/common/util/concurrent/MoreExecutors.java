package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Supplier;
import com.dfsek.terra.lib.google.common.collect.Lists;
import com.dfsek.terra.lib.google.common.collect.Queues;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@GwtCompatible(emulated = true)
public final class MoreExecutors {
   private MoreExecutors() {
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ExecutorService getExitingExecutorService(ThreadPoolExecutor executor, Duration terminationTimeout) {
      return getExitingExecutorService(executor, Internal.toNanosSaturated(terminationTimeout), TimeUnit.NANOSECONDS);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ExecutorService getExitingExecutorService(ThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
      return new MoreExecutors.Application().getExitingExecutorService(executor, terminationTimeout, timeUnit);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ExecutorService getExitingExecutorService(ThreadPoolExecutor executor) {
      return new MoreExecutors.Application().getExitingExecutorService(executor);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor, Duration terminationTimeout) {
      return getExitingScheduledExecutorService(executor, Internal.toNanosSaturated(terminationTimeout), TimeUnit.NANOSECONDS);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
      return new MoreExecutors.Application().getExitingScheduledExecutorService(executor, terminationTimeout, timeUnit);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor) {
      return new MoreExecutors.Application().getExitingScheduledExecutorService(executor);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static void addDelayedShutdownHook(ExecutorService service, Duration terminationTimeout) {
      addDelayedShutdownHook(service, Internal.toNanosSaturated(terminationTimeout), TimeUnit.NANOSECONDS);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static void addDelayedShutdownHook(ExecutorService service, long terminationTimeout, TimeUnit timeUnit) {
      new MoreExecutors.Application().addDelayedShutdownHook(service, terminationTimeout, timeUnit);
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static void useDaemonThreadFactory(ThreadPoolExecutor executor) {
      executor.setThreadFactory(new ThreadFactoryBuilder().setDaemon(true).setThreadFactory(executor.getThreadFactory()).build());
   }

   @GwtIncompatible
   public static ListeningExecutorService newDirectExecutorService() {
      return new DirectExecutorService();
   }

   public static Executor directExecutor() {
      return DirectExecutor.INSTANCE;
   }

   @GwtIncompatible
   public static Executor newSequentialExecutor(Executor delegate) {
      return new SequentialExecutor(delegate);
   }

   @GwtIncompatible
   public static ListeningExecutorService listeningDecorator(ExecutorService delegate) {
      return delegate instanceof ListeningExecutorService
         ? (ListeningExecutorService)delegate
         : (
            delegate instanceof ScheduledExecutorService
               ? new MoreExecutors.ScheduledListeningDecorator((ScheduledExecutorService)delegate)
               : new MoreExecutors.ListeningDecorator(delegate)
         );
   }

   @GwtIncompatible
   public static ListeningScheduledExecutorService listeningDecorator(ScheduledExecutorService delegate) {
      return delegate instanceof ListeningScheduledExecutorService
         ? (ListeningScheduledExecutorService)delegate
         : new MoreExecutors.ScheduledListeningDecorator(delegate);
   }

   @J2ktIncompatible
   @GwtIncompatible
   @ParametricNullness
   static <T> T invokeAnyImpl(ListeningExecutorService executorService, Collection<? extends Callable<T>> tasks, boolean timed, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
      return invokeAnyImpl(executorService, tasks, timed, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   @J2ktIncompatible
   @GwtIncompatible
   @ParametricNullness
   static <T> T invokeAnyImpl(ListeningExecutorService executorService, Collection<? extends Callable<T>> tasks, boolean timed, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      Preconditions.checkNotNull(executorService);
      Preconditions.checkNotNull(unit);
      int ntasks = tasks.size();
      Preconditions.checkArgument(ntasks > 0);
      List<Future<T>> futures = Lists.newArrayListWithCapacity(ntasks);
      BlockingQueue<Future<T>> futureQueue = Queues.newLinkedBlockingQueue();
      long timeoutNanos = unit.toNanos(timeout);

      try {
         ExecutionException ee = null;
         long lastTime = timed ? System.nanoTime() : 0L;
         Iterator<? extends Callable<T>> it = tasks.iterator();
         futures.add(submitAndAddQueueListener(executorService, (Callable<T>)it.next(), futureQueue));
         ntasks--;
         int active = 1;

         while (true) {
            Future<T> f = futureQueue.poll();
            if (f == null) {
               if (ntasks > 0) {
                  ntasks--;
                  futures.add(submitAndAddQueueListener(executorService, (Callable<T>)it.next(), futureQueue));
                  active++;
               } else {
                  if (active == 0) {
                     if (ee == null) {
                        ee = new ExecutionException(null);
                     }

                     throw ee;
                  }

                  if (timed) {
                     f = futureQueue.poll(timeoutNanos, TimeUnit.NANOSECONDS);
                     if (f == null) {
                        throw new TimeoutException();
                     }

                     long now = System.nanoTime();
                     timeoutNanos -= now - lastTime;
                     lastTime = now;
                  } else {
                     f = futureQueue.take();
                  }
               }
            }

            if (f != null) {
               active--;

               try {
                  return f.get();
               } catch (ExecutionException eex) {
                  ee = eex;
               } catch (InterruptedException iex) {
                  throw iex;
               } catch (Exception rex) {
                  ee = new ExecutionException(rex);
               }
            }
         }
      } finally {
         for (Future<T> f : futures) {
            f.cancel(true);
         }
      }
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static <T> ListenableFuture<T> submitAndAddQueueListener(ListeningExecutorService executorService, Callable<T> task, BlockingQueue<Future<T>> queue) {
      ListenableFuture<T> future = executorService.submit(task);
      future.addListener(() -> queue.add(future), directExecutor());
      return future;
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ThreadFactory platformThreadFactory() {
      if (!isAppEngineWithApiClasses()) {
         return Executors.defaultThreadFactory();
      }

      try {
         return (ThreadFactory)Class.forName("com.google.appengine.api.ThreadManager").getMethod("currentRequestThreadFactory").invoke(null);
      } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
         throw new RuntimeException("Couldn't invoke ThreadManager.currentRequestThreadFactory", e);
      } catch (InvocationTargetException e) {
         throw SneakyThrows.sneakyThrow(e.getCause());
      }
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static boolean isAppEngineWithApiClasses() {
      if (System.getProperty("com.google.appengine.runtime.environment") == null) {
         return false;
      }

      try {
         Class.forName("com.google.appengine.api.utils.SystemProperty");
      } catch (ClassNotFoundException e) {
         return false;
      }

      try {
         return Class.forName("com.google.apphosting.api.ApiProxy").getMethod("getCurrentEnvironment").invoke(null) != null;
      } catch (ClassNotFoundException e) {
         return false;
      } catch (InvocationTargetException e) {
         return false;
      } catch (IllegalAccessException e) {
         return false;
      } catch (NoSuchMethodException e) {
         return false;
      }
   }

   @J2ktIncompatible
   @GwtIncompatible
   static Thread newThread(String name, Runnable runnable) {
      Preconditions.checkNotNull(name);
      Preconditions.checkNotNull(runnable);
      Thread result = Objects.requireNonNull(platformThreadFactory().newThread(runnable));

      try {
         result.setName(name);
      } catch (SecurityException var4) {
      }

      return result;
   }

   @J2ktIncompatible
   @GwtIncompatible
   static Executor renamingDecorator(Executor executor, Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(executor);
      Preconditions.checkNotNull(nameSupplier);
      return command -> executor.execute(Callables.threadRenaming(command, nameSupplier));
   }

   @J2ktIncompatible
   @GwtIncompatible
   static ExecutorService renamingDecorator(ExecutorService service, Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(service);
      Preconditions.checkNotNull(nameSupplier);
      return new WrappingExecutorService(service) {
         @Override
         protected <T> Callable<T> wrapTask(Callable<T> callable) {
            return Callables.threadRenaming(callable, nameSupplier);
         }

         @Override
         protected Runnable wrapTask(Runnable command) {
            return Callables.threadRenaming(command, nameSupplier);
         }
      };
   }

   @J2ktIncompatible
   @GwtIncompatible
   static ScheduledExecutorService renamingDecorator(ScheduledExecutorService service, Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(service);
      Preconditions.checkNotNull(nameSupplier);
      return new WrappingScheduledExecutorService(service) {
         @Override
         protected <T> Callable<T> wrapTask(Callable<T> callable) {
            return Callables.threadRenaming(callable, nameSupplier);
         }

         @Override
         protected Runnable wrapTask(Runnable command) {
            return Callables.threadRenaming(command, nameSupplier);
         }
      };
   }

   @CanIgnoreReturnValue
   @J2ktIncompatible
   @GwtIncompatible
   public static boolean shutdownAndAwaitTermination(ExecutorService service, Duration timeout) {
      return shutdownAndAwaitTermination(service, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   @CanIgnoreReturnValue
   @J2ktIncompatible
   @GwtIncompatible
   public static boolean shutdownAndAwaitTermination(ExecutorService service, long timeout, TimeUnit unit) {
      long halfTimeoutNanos = unit.toNanos(timeout) / 2L;
      service.shutdown();

      try {
         if (!service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS)) {
            service.shutdownNow();
            service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS);
         }
      } catch (InterruptedException ie) {
         Thread.currentThread().interrupt();
         service.shutdownNow();
      }

      return service.isTerminated();
   }

   static Executor rejectionPropagatingExecutor(Executor delegate, AbstractFuture<?> future) {
      Preconditions.checkNotNull(delegate);
      Preconditions.checkNotNull(future);
      return delegate == directExecutor() ? delegate : command -> {
         try {
            delegate.execute(command);
         } catch (RejectedExecutionException e) {
            future.setException(e);
         }
      };
   }

   @J2ktIncompatible
   @GwtIncompatible
   @VisibleForTesting
   static class Application {
      final ExecutorService getExitingExecutorService(ThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
         MoreExecutors.useDaemonThreadFactory(executor);
         ExecutorService service = Executors.unconfigurableExecutorService(executor);
         this.addDelayedShutdownHook(executor, terminationTimeout, timeUnit);
         return service;
      }

      final ExecutorService getExitingExecutorService(ThreadPoolExecutor executor) {
         return this.getExitingExecutorService(executor, 120L, TimeUnit.SECONDS);
      }

      final ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
         MoreExecutors.useDaemonThreadFactory(executor);
         ScheduledExecutorService service = Executors.unconfigurableScheduledExecutorService(executor);
         this.addDelayedShutdownHook(executor, terminationTimeout, timeUnit);
         return service;
      }

      final ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor) {
         return this.getExitingScheduledExecutorService(executor, 120L, TimeUnit.SECONDS);
      }

      final void addDelayedShutdownHook(ExecutorService service, long terminationTimeout, TimeUnit timeUnit) {
         Preconditions.checkNotNull(service);
         Preconditions.checkNotNull(timeUnit);
         this.addShutdownHook(MoreExecutors.newThread("DelayedShutdownHook-for-" + service, () -> {
            service.shutdown();

            try {
               service.awaitTermination(terminationTimeout, timeUnit);
            } catch (InterruptedException var5) {
            }
         }));
      }

      @VisibleForTesting
      void addShutdownHook(Thread hook) {
         Runtime.getRuntime().addShutdownHook(hook);
      }
   }

   @GwtIncompatible
   private static class ListeningDecorator extends AbstractListeningExecutorService {
      private final ExecutorService delegate;

      ListeningDecorator(ExecutorService delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      public final boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
         return this.delegate.awaitTermination(timeout, unit);
      }

      @Override
      public final boolean isShutdown() {
         return this.delegate.isShutdown();
      }

      @Override
      public final boolean isTerminated() {
         return this.delegate.isTerminated();
      }

      @Override
      public final void shutdown() {
         this.delegate.shutdown();
      }

      @Override
      public final List<Runnable> shutdownNow() {
         return this.delegate.shutdownNow();
      }

      @Override
      public final void execute(Runnable command) {
         this.delegate.execute(command);
      }

      @Override
      public final String toString() {
         return super.toString() + "[" + this.delegate + "]";
      }
   }

   @GwtIncompatible
   private static final class ScheduledListeningDecorator extends MoreExecutors.ListeningDecorator implements ListeningScheduledExecutorService {
      final ScheduledExecutorService delegate;

      ScheduledListeningDecorator(ScheduledExecutorService delegate) {
         super(delegate);
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      public ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
         TrustedListenableFutureTask<Void> task = TrustedListenableFutureTask.create(command, null);
         ScheduledFuture<?> scheduled = this.delegate.schedule(task, delay, unit);
         return new MoreExecutors.ScheduledListeningDecorator.ListenableScheduledTask<>(task, scheduled);
      }

      @Override
      public <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
         TrustedListenableFutureTask<V> task = TrustedListenableFutureTask.create(callable);
         ScheduledFuture<?> scheduled = this.delegate.schedule(task, delay, unit);
         return new MoreExecutors.ScheduledListeningDecorator.ListenableScheduledTask<>(task, scheduled);
      }

      @Override
      public ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
         MoreExecutors.ScheduledListeningDecorator.NeverSuccessfulListenableFutureTask task = new MoreExecutors.ScheduledListeningDecorator.NeverSuccessfulListenableFutureTask(
            command
         );
         ScheduledFuture<?> scheduled = this.delegate.scheduleAtFixedRate(task, initialDelay, period, unit);
         return new MoreExecutors.ScheduledListeningDecorator.ListenableScheduledTask<>(task, scheduled);
      }

      @Override
      public ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
         MoreExecutors.ScheduledListeningDecorator.NeverSuccessfulListenableFutureTask task = new MoreExecutors.ScheduledListeningDecorator.NeverSuccessfulListenableFutureTask(
            command
         );
         ScheduledFuture<?> scheduled = this.delegate.scheduleWithFixedDelay(task, initialDelay, delay, unit);
         return new MoreExecutors.ScheduledListeningDecorator.ListenableScheduledTask<>(task, scheduled);
      }

      private static final class ListenableScheduledTask<V>
         extends ForwardingListenableFuture.SimpleForwardingListenableFuture<V>
         implements ListenableScheduledFuture<V> {
         private final ScheduledFuture<?> scheduledDelegate;

         public ListenableScheduledTask(ListenableFuture<V> listenableDelegate, ScheduledFuture<?> scheduledDelegate) {
            super(listenableDelegate);
            this.scheduledDelegate = scheduledDelegate;
         }

         @Override
         public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            if (cancelled) {
               this.scheduledDelegate.cancel(mayInterruptIfRunning);
            }

            return cancelled;
         }

         @Override
         public long getDelay(TimeUnit unit) {
            return this.scheduledDelegate.getDelay(unit);
         }

         public int compareTo(Delayed other) {
            return this.scheduledDelegate.compareTo(other);
         }
      }

      @GwtIncompatible
      private static final class NeverSuccessfulListenableFutureTask extends AbstractFuture.TrustedFuture<Void> implements Runnable {
         private final Runnable delegate;

         public NeverSuccessfulListenableFutureTask(Runnable delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
         }

         @Override
         public void run() {
            try {
               this.delegate.run();
            } catch (Throwable t) {
               this.setException(t);
               throw t;
            }
         }

         @Override
         protected String pendingToString() {
            return "task=[" + this.delegate + "]";
         }
      }
   }
}
