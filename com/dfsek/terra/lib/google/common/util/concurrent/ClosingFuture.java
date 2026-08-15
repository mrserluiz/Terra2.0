package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Functions;
import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.FluentIterable;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import com.google.j2objc.annotations.RetainedWith;
import java.io.Closeable;
import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ClosingFuture.from(Futures.immediate*Future)")
@J2ktIncompatible
public final class ClosingFuture<V> {
   private static final LazyLogger logger = new LazyLogger(ClosingFuture.class);
   private final AtomicReference<ClosingFuture.State> state = new AtomicReference<>(ClosingFuture.State.OPEN);
   private final ClosingFuture.CloseableList closeables;
   private final FluentFuture<V> future;

   public static <V> ClosingFuture<V> submit(ClosingFuture.ClosingCallable<V> callable, Executor executor) {
      Preconditions.checkNotNull(callable);
      final ClosingFuture.CloseableList closeables = new ClosingFuture.CloseableList();
      TrustedListenableFutureTask<V> task = TrustedListenableFutureTask.create(new Callable<V>() {
         @ParametricNullness
         @Override
         public V call() throws Exception {
            return callable.call(closeables.closer);
         }

         @Override
         public String toString() {
            return callable.toString();
         }
      });
      executor.execute(task);
      return new ClosingFuture<>(task, closeables);
   }

   public static <V> ClosingFuture<V> submitAsync(ClosingFuture.AsyncClosingCallable<V> callable, Executor executor) {
      Preconditions.checkNotNull(callable);
      final ClosingFuture.CloseableList closeables = new ClosingFuture.CloseableList();
      TrustedListenableFutureTask<V> task = TrustedListenableFutureTask.create(new AsyncCallable<V>() {
         @Override
         public ListenableFuture<V> call() throws Exception {
            ClosingFuture.CloseableList newCloseables = new ClosingFuture.CloseableList();

            try {
               ClosingFuture<V> closingFuture = callable.call(newCloseables.closer);
               closingFuture.becomeSubsumedInto(closeables);
               return closingFuture.future;
            } finally {
               closeables.add(newCloseables, MoreExecutors.directExecutor());
            }
         }

         @Override
         public String toString() {
            return callable.toString();
         }
      });
      executor.execute(task);
      return new ClosingFuture<>(task, closeables);
   }

   public static <V> ClosingFuture<V> from(ListenableFuture<V> future) {
      return new ClosingFuture<>(future);
   }

   @Deprecated
   public static <C extends Object & AutoCloseable> ClosingFuture<C> eventuallyClosing(ListenableFuture<C> future, Executor closingExecutor) {
      Preconditions.checkNotNull(closingExecutor);
      final ClosingFuture<C> closingFuture = new ClosingFuture<>(Futures.nonCancellationPropagating(future));
      Futures.addCallback(future, new FutureCallback<AutoCloseable>() {
         public void onSuccess(@Nullable AutoCloseable result) {
            closingFuture.closeables.closer.eventuallyClose(result, closingExecutor);
         }

         @Override
         public void onFailure(Throwable t) {
         }
      }, MoreExecutors.directExecutor());
      return closingFuture;
   }

   public static ClosingFuture.Combiner whenAllComplete(Iterable<? extends ClosingFuture<?>> futures) {
      return new ClosingFuture.Combiner(false, futures);
   }

   public static ClosingFuture.Combiner whenAllComplete(ClosingFuture<?> future1, ClosingFuture<?>... moreFutures) {
      return whenAllComplete(Lists.asList(future1, moreFutures));
   }

   public static ClosingFuture.Combiner whenAllSucceed(Iterable<? extends ClosingFuture<?>> futures) {
      return new ClosingFuture.Combiner(true, futures);
   }

   public static <V1, V2> ClosingFuture.Combiner2<V1, V2> whenAllSucceed(ClosingFuture<V1> future1, ClosingFuture<V2> future2) {
      return new ClosingFuture.Combiner2<>(future1, future2);
   }

   public static <V1, V2, V3> ClosingFuture.Combiner3<V1, V2, V3> whenAllSucceed(
      ClosingFuture<V1> future1, ClosingFuture<V2> future2, ClosingFuture<V3> future3
   ) {
      return new ClosingFuture.Combiner3<>(future1, future2, future3);
   }

   public static <V1, V2, V3, V4> ClosingFuture.Combiner4<V1, V2, V3, V4> whenAllSucceed(
      ClosingFuture<V1> future1, ClosingFuture<V2> future2, ClosingFuture<V3> future3, ClosingFuture<V4> future4
   ) {
      return new ClosingFuture.Combiner4<>(future1, future2, future3, future4);
   }

   public static <V1, V2, V3, V4, V5> ClosingFuture.Combiner5<V1, V2, V3, V4, V5> whenAllSucceed(
      ClosingFuture<V1> future1, ClosingFuture<V2> future2, ClosingFuture<V3> future3, ClosingFuture<V4> future4, ClosingFuture<V5> future5
   ) {
      return new ClosingFuture.Combiner5<>(future1, future2, future3, future4, future5);
   }

   public static ClosingFuture.Combiner whenAllSucceed(
      ClosingFuture<?> future1,
      ClosingFuture<?> future2,
      ClosingFuture<?> future3,
      ClosingFuture<?> future4,
      ClosingFuture<?> future5,
      ClosingFuture<?> future6,
      ClosingFuture<?>... moreFutures
   ) {
      return whenAllSucceed(FluentIterable.of(future1, future2, future3, future4, future5, future6).append(moreFutures));
   }

   private ClosingFuture(ListenableFuture<V> future) {
      this(future, new ClosingFuture.CloseableList());
   }

   private ClosingFuture(ListenableFuture<V> future, ClosingFuture.CloseableList closeables) {
      this.future = FluentFuture.from(future);
      this.closeables = closeables;
   }

   public ListenableFuture<?> statusFuture() {
      return Futures.nonCancellationPropagating(this.future.transform(Functions.constant(null), MoreExecutors.directExecutor()));
   }

   public <U> ClosingFuture<U> transform(ClosingFuture.ClosingFunction<? super V, U> function, Executor executor) {
      Preconditions.checkNotNull(function);
      AsyncFunction<V, U> applyFunction = new AsyncFunction<V, U>() {
         @Override
         public ListenableFuture<U> apply(V input) throws Exception {
            return ClosingFuture.this.closeables.applyClosingFunction(function, input);
         }

         @Override
         public String toString() {
            return function.toString();
         }
      };
      return this.derive(this.future.transformAsync(applyFunction, executor));
   }

   public <U> ClosingFuture<U> transformAsync(ClosingFuture.AsyncClosingFunction<? super V, U> function, Executor executor) {
      Preconditions.checkNotNull(function);
      AsyncFunction<V, U> applyFunction = new AsyncFunction<V, U>() {
         @Override
         public ListenableFuture<U> apply(V input) throws Exception {
            return ClosingFuture.this.closeables.applyAsyncClosingFunction(function, input);
         }

         @Override
         public String toString() {
            return function.toString();
         }
      };
      return this.derive(this.future.transformAsync(applyFunction, executor));
   }

   public static <V, U> ClosingFuture.AsyncClosingFunction<V, U> withoutCloser(AsyncFunction<V, U> function) {
      Preconditions.checkNotNull(function);
      return (closer, input) -> from(function.apply(input));
   }

   public <X extends Throwable> ClosingFuture<V> catching(
      Class<X> exceptionType, ClosingFuture.ClosingFunction<? super X, ? extends V> fallback, Executor executor
   ) {
      return this.catchingMoreGeneric(exceptionType, fallback, executor);
   }

   private <X extends Throwable, W extends V> ClosingFuture<V> catchingMoreGeneric(
      Class<X> exceptionType, ClosingFuture.ClosingFunction<? super X, W> fallback, Executor executor
   ) {
      Preconditions.checkNotNull(fallback);
      AsyncFunction<X, W> applyFallback = new AsyncFunction<X, W>() {
         public ListenableFuture<W> apply(X exception) throws Exception {
            return ClosingFuture.this.closeables.applyClosingFunction(fallback, exception);
         }

         @Override
         public String toString() {
            return fallback.toString();
         }
      };
      return this.derive(this.future.catchingAsync(exceptionType, applyFallback, executor));
   }

   public <X extends Throwable> ClosingFuture<V> catchingAsync(
      Class<X> exceptionType, ClosingFuture.AsyncClosingFunction<? super X, ? extends V> fallback, Executor executor
   ) {
      return this.catchingAsyncMoreGeneric(exceptionType, fallback, executor);
   }

   private <X extends Throwable, W extends V> ClosingFuture<V> catchingAsyncMoreGeneric(
      Class<X> exceptionType, ClosingFuture.AsyncClosingFunction<? super X, W> fallback, Executor executor
   ) {
      Preconditions.checkNotNull(fallback);
      AsyncFunction<X, W> asyncFunction = new AsyncFunction<X, W>() {
         public ListenableFuture<W> apply(X exception) throws Exception {
            return ClosingFuture.this.closeables.applyAsyncClosingFunction(fallback, exception);
         }

         @Override
         public String toString() {
            return fallback.toString();
         }
      };
      return this.derive(this.future.catchingAsync(exceptionType, asyncFunction, executor));
   }

   public FluentFuture<V> finishToFuture() {
      if (this.compareAndUpdateState(ClosingFuture.State.OPEN, ClosingFuture.State.WILL_CLOSE)) {
         logger.get().log(Level.FINER, "will close {0}", this);
         this.future.addListener(() -> {
            this.checkAndUpdateState(ClosingFuture.State.WILL_CLOSE, ClosingFuture.State.CLOSING);
            this.close();
            this.checkAndUpdateState(ClosingFuture.State.CLOSING, ClosingFuture.State.CLOSED);
         }, MoreExecutors.directExecutor());
      } else {
         switch ((ClosingFuture.State)this.state.get()) {
            case OPEN:
               throw new AssertionError();
            case SUBSUMED:
               throw new IllegalStateException("Cannot call finishToFuture() after deriving another step");
            case WILL_CLOSE:
            case CLOSING:
            case CLOSED:
               throw new IllegalStateException("Cannot call finishToFuture() twice");
            case WILL_CREATE_VALUE_AND_CLOSER:
               throw new IllegalStateException("Cannot call finishToFuture() after calling finishToValueAndCloser()");
         }
      }

      return this.future;
   }

   public void finishToValueAndCloser(ClosingFuture.ValueAndCloserConsumer<? super V> consumer, Executor executor) {
      Preconditions.checkNotNull(consumer);
      if (!this.compareAndUpdateState(ClosingFuture.State.OPEN, ClosingFuture.State.WILL_CREATE_VALUE_AND_CLOSER)) {
         switch ((ClosingFuture.State)this.state.get()) {
            case OPEN:
            default:
               throw new AssertionError(this.state);
            case SUBSUMED:
               throw new IllegalStateException("Cannot call finishToValueAndCloser() after deriving another step");
            case WILL_CLOSE:
            case CLOSING:
            case CLOSED:
               throw new IllegalStateException("Cannot call finishToValueAndCloser() after calling finishToFuture()");
            case WILL_CREATE_VALUE_AND_CLOSER:
               throw new IllegalStateException("Cannot call finishToValueAndCloser() twice");
         }
      } else {
         this.future.addListener(() -> provideValueAndCloser(consumer, this), executor);
      }
   }

   private static <C, V extends C> void provideValueAndCloser(ClosingFuture.ValueAndCloserConsumer<C> consumer, ClosingFuture<V> closingFuture) {
      consumer.accept(new ClosingFuture.ValueAndCloser<>(closingFuture));
   }

   @CanIgnoreReturnValue
   public boolean cancel(boolean mayInterruptIfRunning) {
      logger.get().log(Level.FINER, "cancelling {0}", this);
      boolean cancelled = this.future.cancel(mayInterruptIfRunning);
      if (cancelled) {
         this.close();
      }

      return cancelled;
   }

   private void close() {
      logger.get().log(Level.FINER, "closing {0}", this);
      this.closeables.close();
   }

   private <U> ClosingFuture<U> derive(FluentFuture<U> future) {
      ClosingFuture<U> derived = new ClosingFuture<>(future);
      this.becomeSubsumedInto(derived.closeables);
      return derived;
   }

   private void becomeSubsumedInto(ClosingFuture.CloseableList otherCloseables) {
      this.checkAndUpdateState(ClosingFuture.State.OPEN, ClosingFuture.State.SUBSUMED);
      otherCloseables.add(this.closeables, MoreExecutors.directExecutor());
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).add("state", this.state.get()).addValue(this.future).toString();
   }

   @Override
   protected void finalize() {
      if (this.state.get().equals(ClosingFuture.State.OPEN)) {
         logger.get().log(Level.SEVERE, "Uh oh! An open ClosingFuture has leaked and will close: {0}", this);
         FluentFuture var1 = this.finishToFuture();
      }
   }

   private static void closeQuietly(@Nullable AutoCloseable closeable, Executor executor) {
      if (closeable != null) {
         try {
            executor.execute(() -> {
               try {
                  closeable.close();
               } catch (Exception e) {
                  Platform.restoreInterruptIfIsInterruptedException(e);
                  logger.get().log(Level.WARNING, "thrown by close()", e);
               }
            });
         } catch (RejectedExecutionException e) {
            if (logger.get().isLoggable(Level.WARNING)) {
               logger.get().log(Level.WARNING, String.format("while submitting close to %s; will close inline", executor), e);
            }

            closeQuietly(closeable, MoreExecutors.directExecutor());
         }
      }
   }

   private void checkAndUpdateState(ClosingFuture.State oldState, ClosingFuture.State newState) {
      Preconditions.checkState(this.compareAndUpdateState(oldState, newState), "Expected state to be %s, but it was %s", oldState, newState);
   }

   private boolean compareAndUpdateState(ClosingFuture.State oldState, ClosingFuture.State newState) {
      return this.state.compareAndSet(oldState, newState);
   }

   @VisibleForTesting
   CountDownLatch whenClosedCountDown() {
      return this.closeables.whenClosedCountDown();
   }

   @FunctionalInterface
   public interface AsyncClosingCallable<V> {
      ClosingFuture<V> call(ClosingFuture.DeferredCloser closer) throws Exception;
   }

   @FunctionalInterface
   public interface AsyncClosingFunction<T, U> {
      ClosingFuture<U> apply(ClosingFuture.DeferredCloser closer, @ParametricNullness T input) throws Exception;
   }

   private static final class CloseableList extends IdentityHashMap<AutoCloseable, Executor> implements Closeable {
      private final ClosingFuture.DeferredCloser closer = new ClosingFuture.DeferredCloser(this);
      private volatile boolean closed;
      private volatile @Nullable CountDownLatch whenClosed;

      private CloseableList() {
      }

      <V, U> ListenableFuture<U> applyClosingFunction(ClosingFuture.ClosingFunction<? super V, U> transformation, @ParametricNullness V input) throws Exception {
         ClosingFuture.CloseableList newCloseables = new ClosingFuture.CloseableList();

         try {
            return Futures.immediateFuture(transformation.apply(newCloseables.closer, input));
         } finally {
            this.add(newCloseables, MoreExecutors.directExecutor());
         }
      }

      <V, U> FluentFuture<U> applyAsyncClosingFunction(ClosingFuture.AsyncClosingFunction<V, U> transformation, @ParametricNullness V input) throws Exception {
         ClosingFuture.CloseableList newCloseables = new ClosingFuture.CloseableList();

         try {
            ClosingFuture<U> closingFuture = transformation.apply(newCloseables.closer, input);
            closingFuture.becomeSubsumedInto(newCloseables);
            return closingFuture.future;
         } finally {
            this.add(newCloseables, MoreExecutors.directExecutor());
         }
      }

      @Override
      public void close() {
         if (!this.closed) {
            synchronized (this) {
               if (this.closed) {
                  return;
               }

               this.closed = true;
            }

            for (Entry<AutoCloseable, Executor> entry : this.entrySet()) {
               ClosingFuture.closeQuietly(entry.getKey(), entry.getValue());
            }

            this.clear();
            if (this.whenClosed != null) {
               this.whenClosed.countDown();
            }
         }
      }

      void add(@Nullable AutoCloseable closeable, Executor executor) {
         Preconditions.checkNotNull(executor);
         if (closeable != null) {
            synchronized (this) {
               if (!this.closed) {
                  this.put(closeable, executor);
                  return;
               }
            }

            ClosingFuture.closeQuietly(closeable, executor);
         }
      }

      CountDownLatch whenClosedCountDown() {
         if (this.closed) {
            return new CountDownLatch(0);
         }

         synchronized (this) {
            if (this.closed) {
               return new CountDownLatch(0);
            }

            Preconditions.checkState(this.whenClosed == null);
            return this.whenClosed = new CountDownLatch(1);
         }
      }
   }

   @FunctionalInterface
   public interface ClosingCallable<V> {
      @ParametricNullness
      V call(ClosingFuture.DeferredCloser closer) throws Exception;
   }

   @FunctionalInterface
   public interface ClosingFunction<T, U> {
      @ParametricNullness
      U apply(ClosingFuture.DeferredCloser closer, @ParametricNullness T input) throws Exception;
   }

   @DoNotMock("Use ClosingFuture.whenAllSucceed() or .whenAllComplete() instead.")
   public static class Combiner {
      private final ClosingFuture.CloseableList closeables = new ClosingFuture.CloseableList();
      private final boolean allMustSucceed;
      protected final ImmutableList<ClosingFuture<?>> inputs;

      private Combiner(boolean allMustSucceed, Iterable<? extends ClosingFuture<?>> inputs) {
         this.allMustSucceed = allMustSucceed;
         this.inputs = ImmutableList.copyOf(inputs);

         for (ClosingFuture<?> input : inputs) {
            input.becomeSubsumedInto(this.closeables);
         }
      }

      public <V> ClosingFuture<V> call(ClosingFuture.Combiner.CombiningCallable<V> combiningCallable, Executor executor) {
         Callable<V> callable = new Callable<V>() {
            @ParametricNullness
            @Override
            public V call() throws Exception {
               return new ClosingFuture.Peeker(Combiner.this.inputs).call(combiningCallable, Combiner.this.closeables);
            }

            @Override
            public String toString() {
               return combiningCallable.toString();
            }
         };
         ClosingFuture<V> derived = new ClosingFuture<>(this.futureCombiner().call(callable, executor));
         derived.closeables.add(this.closeables, MoreExecutors.directExecutor());
         return derived;
      }

      public <V> ClosingFuture<V> callAsync(ClosingFuture.Combiner.AsyncCombiningCallable<V> combiningCallable, Executor executor) {
         AsyncCallable<V> asyncCallable = new AsyncCallable<V>() {
            @Override
            public ListenableFuture<V> call() throws Exception {
               return new ClosingFuture.Peeker(Combiner.this.inputs).callAsync(combiningCallable, Combiner.this.closeables);
            }

            @Override
            public String toString() {
               return combiningCallable.toString();
            }
         };
         ClosingFuture<V> derived = new ClosingFuture<>(this.futureCombiner().callAsync(asyncCallable, executor));
         derived.closeables.add(this.closeables, MoreExecutors.directExecutor());
         return derived;
      }

      private Futures.FutureCombiner<@Nullable Object> futureCombiner() {
         return this.allMustSucceed ? Futures.whenAllSucceed(this.inputFutures()) : Futures.whenAllComplete(this.inputFutures());
      }

      private ImmutableList<FluentFuture<?>> inputFutures() {
         return FluentIterable.from(this.inputs).transform(future -> future.future).toList();
      }

      @FunctionalInterface
      public interface AsyncCombiningCallable<V> {
         ClosingFuture<V> call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception;
      }

      @FunctionalInterface
      public interface CombiningCallable<V> {
         @ParametricNullness
         V call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception;
      }
   }

   public static final class Combiner2<V1, V2> extends ClosingFuture.Combiner {
      private final ClosingFuture<V1> future1;
      private final ClosingFuture<V2> future2;

      private Combiner2(ClosingFuture<V1> future1, ClosingFuture<V2> future2) {
         super(true, ImmutableList.of(future1, (ClosingFuture<V1>)future2));
         this.future1 = future1;
         this.future2 = future2;
      }

      public <U> ClosingFuture<U> call(ClosingFuture.Combiner2.ClosingFunction2<V1, V2, U> function, Executor executor) {
         return this.call(new ClosingFuture.Combiner.CombiningCallable<U>() {
            @ParametricNullness
            @Override
            public U call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
               return function.apply(closer, peeker.getDone(Combiner2.this.future1), peeker.getDone(Combiner2.this.future2));
            }

            @Override
            public String toString() {
               return function.toString();
            }
         }, executor);
      }

      public <U> ClosingFuture<U> callAsync(ClosingFuture.Combiner2.AsyncClosingFunction2<V1, V2, U> function, Executor executor) {
         return this.callAsync(new ClosingFuture.Combiner.AsyncCombiningCallable<U>() {
            @Override
            public ClosingFuture<U> call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
               return function.apply(closer, peeker.getDone(Combiner2.this.future1), peeker.getDone(Combiner2.this.future2));
            }

            @Override
            public String toString() {
               return function.toString();
            }
         }, executor);
      }

      @FunctionalInterface
      public interface AsyncClosingFunction2<V1, V2, U> {
         ClosingFuture<U> apply(ClosingFuture.DeferredCloser closer, @ParametricNullness V1 value1, @ParametricNullness V2 value2) throws Exception;
      }

      @FunctionalInterface
      public interface ClosingFunction2<V1, V2, U> {
         @ParametricNullness
         U apply(ClosingFuture.DeferredCloser closer, @ParametricNullness V1 value1, @ParametricNullness V2 value2) throws Exception;
      }
   }

   public static final class Combiner3<V1, V2, V3> extends ClosingFuture.Combiner {
      private final ClosingFuture<V1> future1;
      private final ClosingFuture<V2> future2;
      private final ClosingFuture<V3> future3;

      private Combiner3(ClosingFuture<V1> future1, ClosingFuture<V2> future2, ClosingFuture<V3> future3) {
         super(true, ImmutableList.of(future1, (ClosingFuture<V1>)future2, (ClosingFuture<V1>)future3));
         this.future1 = future1;
         this.future2 = future2;
         this.future3 = future3;
      }

      public <U> ClosingFuture<U> call(ClosingFuture.Combiner3.ClosingFunction3<V1, V2, V3, U> function, Executor executor) {
         return this.call(
            new ClosingFuture.Combiner.CombiningCallable<U>() {
               @ParametricNullness
               @Override
               public U call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
                  return function.apply(
                     closer, peeker.getDone(Combiner3.this.future1), peeker.getDone(Combiner3.this.future2), peeker.getDone(Combiner3.this.future3)
                  );
               }

               @Override
               public String toString() {
                  return function.toString();
               }
            },
            executor
         );
      }

      public <U> ClosingFuture<U> callAsync(ClosingFuture.Combiner3.AsyncClosingFunction3<V1, V2, V3, U> function, Executor executor) {
         return this.callAsync(
            new ClosingFuture.Combiner.AsyncCombiningCallable<U>() {
               @Override
               public ClosingFuture<U> call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
                  return function.apply(
                     closer, peeker.getDone(Combiner3.this.future1), peeker.getDone(Combiner3.this.future2), peeker.getDone(Combiner3.this.future3)
                  );
               }

               @Override
               public String toString() {
                  return function.toString();
               }
            },
            executor
         );
      }

      @FunctionalInterface
      public interface AsyncClosingFunction3<V1, V2, V3, U> {
         ClosingFuture<U> apply(
            ClosingFuture.DeferredCloser closer, @ParametricNullness V1 value1, @ParametricNullness V2 value2, @ParametricNullness V3 value3
         ) throws Exception;
      }

      @FunctionalInterface
      public interface ClosingFunction3<V1, V2, V3, U> {
         @ParametricNullness
         U apply(ClosingFuture.DeferredCloser closer, @ParametricNullness V1 value1, @ParametricNullness V2 value2, @ParametricNullness V3 value3) throws Exception;
      }
   }

   public static final class Combiner4<V1, V2, V3, V4> extends ClosingFuture.Combiner {
      private final ClosingFuture<V1> future1;
      private final ClosingFuture<V2> future2;
      private final ClosingFuture<V3> future3;
      private final ClosingFuture<V4> future4;

      private Combiner4(ClosingFuture<V1> future1, ClosingFuture<V2> future2, ClosingFuture<V3> future3, ClosingFuture<V4> future4) {
         super(true, ImmutableList.of(future1, (ClosingFuture<V1>)future2, (ClosingFuture<V1>)future3, (ClosingFuture<V1>)future4));
         this.future1 = future1;
         this.future2 = future2;
         this.future3 = future3;
         this.future4 = future4;
      }

      public <U> ClosingFuture<U> call(ClosingFuture.Combiner4.ClosingFunction4<V1, V2, V3, V4, U> function, Executor executor) {
         return this.call(
            new ClosingFuture.Combiner.CombiningCallable<U>() {
               @ParametricNullness
               @Override
               public U call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
                  return function.apply(
                     closer,
                     peeker.getDone(Combiner4.this.future1),
                     peeker.getDone(Combiner4.this.future2),
                     peeker.getDone(Combiner4.this.future3),
                     peeker.getDone(Combiner4.this.future4)
                  );
               }

               @Override
               public String toString() {
                  return function.toString();
               }
            },
            executor
         );
      }

      public <U> ClosingFuture<U> callAsync(ClosingFuture.Combiner4.AsyncClosingFunction4<V1, V2, V3, V4, U> function, Executor executor) {
         return this.callAsync(
            new ClosingFuture.Combiner.AsyncCombiningCallable<U>() {
               @Override
               public ClosingFuture<U> call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
                  return function.apply(
                     closer,
                     peeker.getDone(Combiner4.this.future1),
                     peeker.getDone(Combiner4.this.future2),
                     peeker.getDone(Combiner4.this.future3),
                     peeker.getDone(Combiner4.this.future4)
                  );
               }

               @Override
               public String toString() {
                  return function.toString();
               }
            },
            executor
         );
      }

      @FunctionalInterface
      public interface AsyncClosingFunction4<V1, V2, V3, V4, U> {
         ClosingFuture<U> apply(
            ClosingFuture.DeferredCloser closer,
            @ParametricNullness V1 value1,
            @ParametricNullness V2 value2,
            @ParametricNullness V3 value3,
            @ParametricNullness V4 value4
         ) throws Exception;
      }

      @FunctionalInterface
      public interface ClosingFunction4<V1, V2, V3, V4, U> {
         @ParametricNullness
         U apply(
            ClosingFuture.DeferredCloser closer,
            @ParametricNullness V1 value1,
            @ParametricNullness V2 value2,
            @ParametricNullness V3 value3,
            @ParametricNullness V4 value4
         ) throws Exception;
      }
   }

   public static final class Combiner5<V1, V2, V3, V4, V5> extends ClosingFuture.Combiner {
      private final ClosingFuture<V1> future1;
      private final ClosingFuture<V2> future2;
      private final ClosingFuture<V3> future3;
      private final ClosingFuture<V4> future4;
      private final ClosingFuture<V5> future5;

      private Combiner5(ClosingFuture<V1> future1, ClosingFuture<V2> future2, ClosingFuture<V3> future3, ClosingFuture<V4> future4, ClosingFuture<V5> future5) {
         super(true, ImmutableList.of(future1, (ClosingFuture<V1>)future2, (ClosingFuture<V1>)future3, (ClosingFuture<V1>)future4, (ClosingFuture<V1>)future5));
         this.future1 = future1;
         this.future2 = future2;
         this.future3 = future3;
         this.future4 = future4;
         this.future5 = future5;
      }

      public <U> ClosingFuture<U> call(ClosingFuture.Combiner5.ClosingFunction5<V1, V2, V3, V4, V5, U> function, Executor executor) {
         return this.call(
            new ClosingFuture.Combiner.CombiningCallable<U>() {
               @ParametricNullness
               @Override
               public U call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
                  return function.apply(
                     closer,
                     peeker.getDone(Combiner5.this.future1),
                     peeker.getDone(Combiner5.this.future2),
                     peeker.getDone(Combiner5.this.future3),
                     peeker.getDone(Combiner5.this.future4),
                     peeker.getDone(Combiner5.this.future5)
                  );
               }

               @Override
               public String toString() {
                  return function.toString();
               }
            },
            executor
         );
      }

      public <U> ClosingFuture<U> callAsync(ClosingFuture.Combiner5.AsyncClosingFunction5<V1, V2, V3, V4, V5, U> function, Executor executor) {
         return this.callAsync(
            new ClosingFuture.Combiner.AsyncCombiningCallable<U>() {
               @Override
               public ClosingFuture<U> call(ClosingFuture.DeferredCloser closer, ClosingFuture.Peeker peeker) throws Exception {
                  return function.apply(
                     closer,
                     peeker.getDone(Combiner5.this.future1),
                     peeker.getDone(Combiner5.this.future2),
                     peeker.getDone(Combiner5.this.future3),
                     peeker.getDone(Combiner5.this.future4),
                     peeker.getDone(Combiner5.this.future5)
                  );
               }

               @Override
               public String toString() {
                  return function.toString();
               }
            },
            executor
         );
      }

      @FunctionalInterface
      public interface AsyncClosingFunction5<V1, V2, V3, V4, V5, U> {
         ClosingFuture<U> apply(
            ClosingFuture.DeferredCloser closer,
            @ParametricNullness V1 value1,
            @ParametricNullness V2 value2,
            @ParametricNullness V3 value3,
            @ParametricNullness V4 value4,
            @ParametricNullness V5 value5
         ) throws Exception;
      }

      @FunctionalInterface
      public interface ClosingFunction5<V1, V2, V3, V4, V5, U> {
         @ParametricNullness
         U apply(
            ClosingFuture.DeferredCloser closer,
            @ParametricNullness V1 value1,
            @ParametricNullness V2 value2,
            @ParametricNullness V3 value3,
            @ParametricNullness V4 value4,
            @ParametricNullness V5 value5
         ) throws Exception;
      }
   }

   public static final class DeferredCloser {
      @RetainedWith
      private final ClosingFuture.CloseableList list;

      DeferredCloser(ClosingFuture.CloseableList list) {
         this.list = list;
      }

      @CanIgnoreReturnValue
      @ParametricNullness
      public <C extends Object & AutoCloseable> C eventuallyClose(@ParametricNullness C closeable, Executor closingExecutor) {
         Preconditions.checkNotNull(closingExecutor);
         if (closeable != null) {
            this.list.add(closeable, closingExecutor);
         }

         return closeable;
      }
   }

   public static final class Peeker {
      private final ImmutableList<ClosingFuture<?>> futures;
      private volatile boolean beingCalled;

      private Peeker(ImmutableList<ClosingFuture<?>> futures) {
         this.futures = Preconditions.checkNotNull(futures);
      }

      @ParametricNullness
      public final <D> D getDone(ClosingFuture<D> closingFuture) throws ExecutionException {
         Preconditions.checkState(this.beingCalled);
         Preconditions.checkArgument(this.futures.contains(closingFuture));
         return Futures.getDone(closingFuture.future);
      }

      @ParametricNullness
      private <V> V call(ClosingFuture.Combiner.CombiningCallable<V> combiner, ClosingFuture.CloseableList closeables) throws Exception {
         this.beingCalled = true;
         ClosingFuture.CloseableList newCloseables = new ClosingFuture.CloseableList();

         try {
            return combiner.call(newCloseables.closer, this);
         } finally {
            closeables.add(newCloseables, MoreExecutors.directExecutor());
            this.beingCalled = false;
         }
      }

      private <V> FluentFuture<V> callAsync(ClosingFuture.Combiner.AsyncCombiningCallable<V> combiner, ClosingFuture.CloseableList closeables) throws Exception {
         this.beingCalled = true;
         ClosingFuture.CloseableList newCloseables = new ClosingFuture.CloseableList();

         try {
            ClosingFuture<V> closingFuture = combiner.call(newCloseables.closer, this);
            closingFuture.becomeSubsumedInto(closeables);
            return closingFuture.future;
         } finally {
            closeables.add(newCloseables, MoreExecutors.directExecutor());
            this.beingCalled = false;
         }
      }
   }

   enum State {
      OPEN,
      SUBSUMED,
      WILL_CLOSE,
      CLOSING,
      CLOSED,
      WILL_CREATE_VALUE_AND_CLOSER;
   }

   public static final class ValueAndCloser<V> {
      private final ClosingFuture<? extends V> closingFuture;

      ValueAndCloser(ClosingFuture<? extends V> closingFuture) {
         this.closingFuture = Preconditions.checkNotNull(closingFuture);
      }

      @ParametricNullness
      public V get() throws ExecutionException {
         return Futures.getDone((Future<V>)this.closingFuture.future);
      }

      public void closeAsync() {
         this.closingFuture.close();
      }
   }

   @FunctionalInterface
   public interface ValueAndCloserConsumer<V> {
      void accept(ClosingFuture.ValueAndCloser<V> valueAndCloser);
   }
}
