package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.dfsek.terra.lib.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Futures extends GwtFuturesCatchingSpecialization {
   private Futures() {
   }

   public static <V> ListenableFuture<V> immediateFuture(@ParametricNullness V value) {
      return value == null ? ImmediateFuture.NULL : new ImmediateFuture<>(value);
   }

   public static ListenableFuture<Void> immediateVoidFuture() {
      return (ListenableFuture<Void>)ImmediateFuture.NULL;
   }

   public static <V> ListenableFuture<V> immediateFailedFuture(Throwable throwable) {
      Preconditions.checkNotNull(throwable);
      return new ImmediateFuture.ImmediateFailedFuture<>(throwable);
   }

   public static <V> ListenableFuture<V> immediateCancelledFuture() {
      ListenableFuture<Object> instance = ImmediateFuture.ImmediateCancelledFuture.INSTANCE;
      return instance != null ? instance : new ImmediateFuture.ImmediateCancelledFuture<>();
   }

   public static <O> ListenableFuture<O> submit(Callable<O> callable, Executor executor) {
      TrustedListenableFutureTask<O> task = TrustedListenableFutureTask.create(callable);
      executor.execute(task);
      return task;
   }

   public static ListenableFuture<Void> submit(Runnable runnable, Executor executor) {
      TrustedListenableFutureTask<Void> task = TrustedListenableFutureTask.create(runnable, null);
      executor.execute(task);
      return task;
   }

   public static <O> ListenableFuture<O> submitAsync(AsyncCallable<O> callable, Executor executor) {
      TrustedListenableFutureTask<O> task = TrustedListenableFutureTask.create(callable);
      executor.execute(task);
      return task;
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <O> ListenableFuture<O> scheduleAsync(AsyncCallable<O> callable, Duration delay, ScheduledExecutorService executorService) {
      return scheduleAsync(callable, Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS, executorService);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <O> ListenableFuture<O> scheduleAsync(AsyncCallable<O> callable, long delay, TimeUnit timeUnit, ScheduledExecutorService executorService) {
      TrustedListenableFutureTask<O> task = TrustedListenableFutureTask.create(callable);
      Future<?> scheduled = executorService.schedule(task, delay, timeUnit);
      task.addListener(() -> scheduled.cancel(false), MoreExecutors.directExecutor());
      return task;
   }

   @J2ktIncompatible
   @Partially.GwtIncompatible("AVAILABLE but requires exceptionType to be Throwable.class")
   public static <V, X extends Throwable> ListenableFuture<V> catching(
      ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor
   ) {
      return AbstractCatchingFuture.create(input, exceptionType, fallback, executor);
   }

   @J2ktIncompatible
   @Partially.GwtIncompatible("AVAILABLE but requires exceptionType to be Throwable.class")
   public static <V, X extends Throwable> ListenableFuture<V> catchingAsync(
      ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor
   ) {
      return AbstractCatchingFuture.createAsync(input, exceptionType, fallback, executor);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <V> ListenableFuture<V> withTimeout(ListenableFuture<V> delegate, Duration time, ScheduledExecutorService scheduledExecutor) {
      return withTimeout(delegate, Internal.toNanosSaturated(time), TimeUnit.NANOSECONDS, scheduledExecutor);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <V> ListenableFuture<V> withTimeout(ListenableFuture<V> delegate, long time, TimeUnit unit, ScheduledExecutorService scheduledExecutor) {
      return delegate.isDone() ? delegate : TimeoutFuture.create(delegate, time, unit, scheduledExecutor);
   }

   public static <I, O> ListenableFuture<O> transformAsync(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
      return AbstractTransformFuture.createAsync(input, function, executor);
   }

   public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
      return AbstractTransformFuture.create(input, function, executor);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <I, O> Future<O> lazyTransform(Future<I> input, Function<? super I, ? extends O> function) {
      Preconditions.checkNotNull(input);
      Preconditions.checkNotNull(function);
      return new Future<O>() {
         @Override
         public boolean cancel(boolean mayInterruptIfRunning) {
            return input.cancel(mayInterruptIfRunning);
         }

         @Override
         public boolean isCancelled() {
            return input.isCancelled();
         }

         @Override
         public boolean isDone() {
            return input.isDone();
         }

         @Override
         public O get() throws InterruptedException, ExecutionException {
            return (O)this.applyTransformation(input.get());
         }

         @Override
         public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return (O)this.applyTransformation(input.get(timeout, unit));
         }

         private O applyTransformation(I input) throws ExecutionException {
            try {
               return (O)function.apply(input);
            } catch (Throwable t) {
               throw new ExecutionException(t);
            }
         }
      };
   }

   @SafeVarargs
   public static <V> ListenableFuture<List<V>> allAsList(ListenableFuture<? extends V>... futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), true);
   }

   public static <V> ListenableFuture<List<V>> allAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), true);
   }

   @SafeVarargs
   public static <V> Futures.FutureCombiner<V> whenAllComplete(ListenableFuture<? extends V>... futures) {
      return new Futures.FutureCombiner<>(false, ImmutableList.copyOf(futures));
   }

   public static <V> Futures.FutureCombiner<V> whenAllComplete(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new Futures.FutureCombiner<>(false, ImmutableList.copyOf(futures));
   }

   @SafeVarargs
   public static <V> Futures.FutureCombiner<V> whenAllSucceed(ListenableFuture<? extends V>... futures) {
      return new Futures.FutureCombiner<>(true, ImmutableList.copyOf(futures));
   }

   public static <V> Futures.FutureCombiner<V> whenAllSucceed(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new Futures.FutureCombiner<>(true, ImmutableList.copyOf(futures));
   }

   public static <V> ListenableFuture<V> nonCancellationPropagating(ListenableFuture<V> future) {
      if (future.isDone()) {
         return future;
      }

      Futures.NonCancellationPropagatingFuture<V> output = new Futures.NonCancellationPropagatingFuture<>(future);
      future.addListener(output, MoreExecutors.directExecutor());
      return output;
   }

   @SafeVarargs
   public static <V> ListenableFuture<List<@Nullable V>> successfulAsList(ListenableFuture<? extends V>... futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), false);
   }

   public static <V> ListenableFuture<List<@Nullable V>> successfulAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), false);
   }

   public static <T> ImmutableList<ListenableFuture<T>> inCompletionOrder(Iterable<? extends ListenableFuture<? extends T>> futures) {
      ListenableFuture<? extends T>[] copy = gwtCompatibleToArray(futures);
      Futures.InCompletionOrderState<T> state = new Futures.InCompletionOrderState<>(copy);
      ImmutableList.Builder<AbstractFuture<T>> delegatesBuilder = ImmutableList.builderWithExpectedSize(copy.length);

      for (int i = 0; i < copy.length; i++) {
         delegatesBuilder.add(new Futures.InCompletionOrderFuture<>(state));
      }

      ImmutableList<AbstractFuture<T>> delegates = delegatesBuilder.build();

      for (int i = 0; i < copy.length; i++) {
         int localI = i;
         copy[i].addListener(() -> state.recordInputCompletion(delegates, localI), MoreExecutors.directExecutor());
      }

      return delegates;
   }

   private static <T> ListenableFuture<? extends T>[] gwtCompatibleToArray(Iterable<? extends ListenableFuture<? extends T>> futures) {
      Collection<ListenableFuture<? extends T>> collection;
      if (futures instanceof Collection) {
         collection = (Collection<ListenableFuture<? extends T>>)futures;
      } else {
         collection = ImmutableList.copyOf(futures);
      }

      return collection.toArray(new ListenableFuture[0]);
   }

   public static <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback, Executor executor) {
      Preconditions.checkNotNull(callback);
      future.addListener(new Futures.CallbackListener<>(future, callback), executor);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   public static <V> V getDone(Future<V> future) throws ExecutionException {
      Preconditions.checkState(future.isDone(), "Future was expected to be done: %s", future);
      return Uninterruptibles.getUninterruptibly(future);
   }

   @CanIgnoreReturnValue
   @J2ktIncompatible
   @GwtIncompatible
   @ParametricNullness
   public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass) throws X {
      return FuturesGetChecked.getChecked(future, exceptionClass);
   }

   @CanIgnoreReturnValue
   @J2ktIncompatible
   @GwtIncompatible
   @ParametricNullness
   public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass, Duration timeout) throws X {
      return getChecked(future, exceptionClass, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   @CanIgnoreReturnValue
   @J2ktIncompatible
   @GwtIncompatible
   @ParametricNullness
   public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass, long timeout, TimeUnit unit) throws X {
      return FuturesGetChecked.getChecked(future, exceptionClass, timeout, unit);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   public static <V> V getUnchecked(Future<V> future) {
      Preconditions.checkNotNull(future);

      try {
         return Uninterruptibles.getUninterruptibly(future);
      } catch (ExecutionException wrapper) {
         if (wrapper.getCause() instanceof Error) {
            throw new ExecutionError((Error)wrapper.getCause());
         } else {
            throw new UncheckedExecutionException(wrapper.getCause());
         }
      }
   }

   private static final class CallbackListener<V> implements Runnable {
      final Future<V> future;
      final FutureCallback<? super V> callback;

      CallbackListener(Future<V> future, FutureCallback<? super V> callback) {
         this.future = future;
         this.callback = callback;
      }

      @Override
      public void run() {
         if (this.future instanceof InternalFutureFailureAccess) {
            Throwable failure = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)this.future);
            if (failure != null) {
               this.callback.onFailure(failure);
               return;
            }
         }

         V value;
         try {
            value = Futures.getDone(this.future);
         } catch (ExecutionException e) {
            this.callback.onFailure(e.getCause());
            return;
         } catch (Throwable e) {
            this.callback.onFailure(e);
            return;
         }

         this.callback.onSuccess(value);
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this).addValue(this.callback).toString();
      }
   }

   @GwtCompatible
   public static final class FutureCombiner<V> {
      private final boolean allMustSucceed;
      private final ImmutableList<ListenableFuture<? extends V>> futures;

      private FutureCombiner(boolean allMustSucceed, ImmutableList<ListenableFuture<? extends V>> futures) {
         this.allMustSucceed = allMustSucceed;
         this.futures = futures;
      }

      public <C> ListenableFuture<C> callAsync(AsyncCallable<C> combiner, Executor executor) {
         return new CombinedFuture<>(this.futures, this.allMustSucceed, executor, combiner);
      }

      public <C> ListenableFuture<C> call(Callable<C> combiner, Executor executor) {
         return new CombinedFuture<>(this.futures, this.allMustSucceed, executor, combiner);
      }

      public ListenableFuture<?> run(Runnable combiner, Executor executor) {
         return this.call(new Callable<Void>() {
            public @Nullable Void call() throws Exception {
               combiner.run();
               return null;
            }
         }, executor);
      }
   }

   private static final class InCompletionOrderFuture<T> extends AbstractFuture<T> {
      private Futures.@Nullable InCompletionOrderState<T> state;

      private InCompletionOrderFuture(Futures.InCompletionOrderState<T> state) {
         this.state = state;
      }

      @Override
      public boolean cancel(boolean interruptIfRunning) {
         Futures.InCompletionOrderState<T> localState = this.state;
         if (super.cancel(interruptIfRunning)) {
            Objects.requireNonNull(localState).recordOutputCancellation(interruptIfRunning);
            return true;
         } else {
            return false;
         }
      }

      @Override
      protected void afterDone() {
         this.state = null;
      }

      @Override
      protected @Nullable String pendingToString() {
         Futures.InCompletionOrderState<T> localState = this.state;
         return localState != null ? "inputCount=[" + localState.inputFutures.length + "], remaining=[" + localState.incompleteOutputCount.get() + "]" : null;
      }
   }

   private static final class InCompletionOrderState<T> {
      private boolean wasCancelled = false;
      private boolean shouldInterrupt = true;
      private final AtomicInteger incompleteOutputCount;
      private final @Nullable ListenableFuture<? extends T>[] inputFutures;
      private volatile int delegateIndex = 0;

      private InCompletionOrderState(ListenableFuture<? extends T>[] inputFutures) {
         this.inputFutures = inputFutures;
         this.incompleteOutputCount = new AtomicInteger(inputFutures.length);
      }

      private void recordOutputCancellation(boolean interruptIfRunning) {
         this.wasCancelled = true;
         if (!interruptIfRunning) {
            this.shouldInterrupt = false;
         }

         this.recordCompletion();
      }

      private void recordInputCompletion(ImmutableList<AbstractFuture<T>> delegates, int inputFutureIndex) {
         ListenableFuture<? extends T> inputFuture = Objects.requireNonNull(this.inputFutures[inputFutureIndex]);
         this.inputFutures[inputFutureIndex] = null;

         for (int i = this.delegateIndex; i < delegates.size(); i++) {
            if (delegates.get(i).setFuture(inputFuture)) {
               this.recordCompletion();
               this.delegateIndex = i + 1;
               return;
            }
         }

         this.delegateIndex = delegates.size();
      }

      private void recordCompletion() {
         if (this.incompleteOutputCount.decrementAndGet() == 0 && this.wasCancelled) {
            for (ListenableFuture<? extends T> toCancel : this.inputFutures) {
               if (toCancel != null) {
                  toCancel.cancel(this.shouldInterrupt);
               }
            }
         }
      }
   }

   private static final class NonCancellationPropagatingFuture<V> extends AbstractFuture.TrustedFuture<V> implements Runnable {
      @LazyInit
      private @Nullable ListenableFuture<V> delegate;

      NonCancellationPropagatingFuture(ListenableFuture<V> delegate) {
         this.delegate = delegate;
      }

      @Override
      public void run() {
         ListenableFuture<V> localDelegate = this.delegate;
         if (localDelegate != null) {
            this.setFuture(localDelegate);
         }
      }

      @Override
      protected @Nullable String pendingToString() {
         ListenableFuture<V> localDelegate = this.delegate;
         return localDelegate != null ? "delegate=[" + localDelegate + "]" : null;
      }

      @Override
      protected void afterDone() {
         this.delegate = null;
      }
   }
}
