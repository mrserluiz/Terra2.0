package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.dfsek.terra.lib.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractCatchingFuture<V, X extends Throwable, F, T> extends FluentFuture.TrustedFuture<V> implements Runnable {
   @LazyInit
   @Nullable ListenableFuture<? extends V> inputFuture;
   @LazyInit
   @Nullable Class<X> exceptionType;
   @LazyInit
   @Nullable F fallback;

   static <V, X extends Throwable> ListenableFuture<V> create(
      ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor
   ) {
      AbstractCatchingFuture.CatchingFuture<V, X> output = new AbstractCatchingFuture.CatchingFuture<>(input, exceptionType, fallback);
      input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
      return output;
   }

   static <X extends Throwable, V> ListenableFuture<V> createAsync(
      ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor
   ) {
      AbstractCatchingFuture.AsyncCatchingFuture<V, X> output = new AbstractCatchingFuture.AsyncCatchingFuture<>(input, exceptionType, fallback);
      input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
      return output;
   }

   AbstractCatchingFuture(ListenableFuture<? extends V> inputFuture, Class<X> exceptionType, F fallback) {
      this.inputFuture = Preconditions.checkNotNull(inputFuture);
      this.exceptionType = Preconditions.checkNotNull(exceptionType);
      this.fallback = Preconditions.checkNotNull(fallback);
   }

   @Override
   public final void run() {
      ListenableFuture<? extends V> localInputFuture = this.inputFuture;
      Class<X> localExceptionType = this.exceptionType;
      F localFallback = this.fallback;
      if (!(localInputFuture == null | localExceptionType == null | localFallback == null) && !this.isCancelled()) {
         this.inputFuture = null;
         V sourceResult = null;
         Throwable throwable = null;

         try {
            if (localInputFuture instanceof InternalFutureFailureAccess) {
               throwable = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)localInputFuture);
            }

            if (throwable == null) {
               sourceResult = Futures.getDone((Future<V>)localInputFuture);
            }
         } catch (ExecutionException e) {
            throwable = e.getCause();
            if (throwable == null) {
               throwable = new NullPointerException("Future type " + localInputFuture.getClass() + " threw " + e.getClass() + " without a cause");
            }
         } catch (Throwable t) {
            throwable = t;
         }

         if (throwable == null) {
            this.set(NullnessCasts.uncheckedCastNullableTToT(sourceResult));
         } else if (!Platform.isInstanceOfThrowableClass(throwable, localExceptionType)) {
            this.setFuture(localInputFuture);
         } else {
            X castThrowable = (X)throwable;

            T fallbackResult;
            label91: {
               try {
                  fallbackResult = this.doFallback(localFallback, castThrowable);
                  break label91;
               } catch (Throwable t) {
                  Platform.restoreInterruptIfIsInterruptedException(t);
                  this.setException(t);
               } finally {
                  this.exceptionType = null;
                  this.fallback = null;
               }

               return;
            }

            this.setResult(fallbackResult);
         }
      }
   }

   @ForOverride
   @ParametricNullness
   abstract T doFallback(F fallback, X throwable) throws Exception;

   @ForOverride
   abstract void setResult(@ParametricNullness T result);

   @Override
   protected final void afterDone() {
      ListenableFuture<? extends V> localInputFuture = this.inputFuture;
      this.maybePropagateCancellationTo(localInputFuture);
      this.inputFuture = null;
      this.exceptionType = null;
      this.fallback = null;
   }

   @Override
   protected @Nullable String pendingToString() {
      ListenableFuture<? extends V> localInputFuture = this.inputFuture;
      Class<X> localExceptionType = this.exceptionType;
      F localFallback = this.fallback;
      String superString = super.pendingToString();
      String resultString = "";
      if (localInputFuture != null) {
         resultString = "inputFuture=[" + localInputFuture + "], ";
      }

      if (localExceptionType != null && localFallback != null) {
         return resultString + "exceptionType=[" + localExceptionType + "], fallback=[" + localFallback + "]";
      } else {
         return superString != null ? resultString + superString : null;
      }
   }

   private static final class AsyncCatchingFuture<V, X extends Throwable>
      extends AbstractCatchingFuture<V, X, AsyncFunction<? super X, ? extends V>, ListenableFuture<? extends V>> {
      AsyncCatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback) {
         super(input, exceptionType, fallback);
      }

      ListenableFuture<? extends V> doFallback(AsyncFunction<? super X, ? extends V> fallback, X cause) throws Exception {
         ListenableFuture<? extends V> output = fallback.apply(cause);
         Preconditions.checkNotNull(output, "AsyncFunction.apply returned null instead of a Future. Did you mean to return immediateFuture(null)? %s", fallback);
         return output;
      }

      void setResult(ListenableFuture<? extends V> result) {
         this.setFuture(result);
      }
   }

   private static final class CatchingFuture<V, X extends Throwable> extends AbstractCatchingFuture<V, X, Function<? super X, ? extends V>, V> {
      CatchingFuture(ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback) {
         super(input, exceptionType, fallback);
      }

      @ParametricNullness
      V doFallback(Function<? super X, ? extends V> fallback, X cause) throws Exception {
         return (V)fallback.apply(cause);
      }

      @Override
      void setResult(@ParametricNullness V result) {
         this.set(result);
      }
   }
}
