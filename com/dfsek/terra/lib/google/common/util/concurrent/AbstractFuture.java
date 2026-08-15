package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Strings;
import com.dfsek.terra.lib.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.dfsek.terra.lib.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.j2objc.annotations.ReflectionSupport;
import com.google.j2objc.annotations.ReflectionSupport.Level;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
@ReflectionSupport(Level.FULL)
public abstract class AbstractFuture<V> extends AbstractFutureState<V> {
   protected AbstractFuture() {
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
      return Platform.get(this, timeout, unit);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public V get() throws InterruptedException, ExecutionException {
      return Platform.get(this);
   }

   @ParametricNullness
   final V getFromAlreadyDoneTrustedFuture() throws ExecutionException {
      Object localValue = this.value();
      if (localValue == null | localValue instanceof AbstractFuture.DelegatingToFuture) {
         throw new IllegalStateException("Cannot get() on a pending future.");
      } else {
         return getDoneValue(localValue);
      }
   }

   @ParametricNullness
   static <V> V getDoneValue(Object obj) throws ExecutionException {
      if (obj instanceof AbstractFuture.Cancellation) {
         AbstractFuture.Cancellation cancellation = (AbstractFuture.Cancellation)obj;
         Throwable cause = cancellation.cause;
         throw cancellationExceptionWithCause("Task was cancelled.", cause);
      } else if (obj instanceof AbstractFuture.Failure) {
         AbstractFuture.Failure failure = (AbstractFuture.Failure)obj;
         Throwable exception = failure.exception;
         throw new ExecutionException(exception);
      } else {
         return (V)(obj == NULL ? NullnessCasts.uncheckedNull() : obj);
      }
   }

   static boolean notInstanceOfDelegatingToFuture(@Nullable Object obj) {
      return !(obj instanceof AbstractFuture.DelegatingToFuture);
   }

   @Override
   public boolean isDone() {
      Object localValue = this.value();
      return localValue != null & notInstanceOfDelegatingToFuture(localValue);
   }

   @Override
   public boolean isCancelled() {
      Object localValue = this.value();
      return localValue instanceof AbstractFuture.Cancellation;
   }

   @CanIgnoreReturnValue
   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      Object localValue = this.value();
      boolean rValue = false;
      if (localValue == null | localValue instanceof AbstractFuture.DelegatingToFuture) {
         Object valueToSet = GENERATE_CANCELLATION_CAUSES
            ? new AbstractFuture.Cancellation(mayInterruptIfRunning, new CancellationException("Future.cancel() was called."))
            : Objects.requireNonNull(
               mayInterruptIfRunning ? AbstractFuture.Cancellation.CAUSELESS_INTERRUPTED : AbstractFuture.Cancellation.CAUSELESS_CANCELLED
            );
         AbstractFuture<?> abstractFuture = this;

         while (true) {
            while (!casValue(abstractFuture, localValue, valueToSet)) {
               localValue = abstractFuture.value();
               if (notInstanceOfDelegatingToFuture(localValue)) {
                  return rValue;
               }
            }

            rValue = true;
            complete(abstractFuture, mayInterruptIfRunning);
            if (!(localValue instanceof AbstractFuture.DelegatingToFuture)) {
               break;
            }

            ListenableFuture<?> futureToPropagateTo = ((AbstractFuture.DelegatingToFuture)localValue).future;
            if (!(futureToPropagateTo instanceof AbstractFuture.Trusted)) {
               futureToPropagateTo.cancel(mayInterruptIfRunning);
               break;
            }

            AbstractFuture<?> trusted = (AbstractFuture<?>)futureToPropagateTo;
            localValue = trusted.value();
            if (!(localValue == null | localValue instanceof AbstractFuture.DelegatingToFuture)) {
               break;
            }

            abstractFuture = trusted;
         }
      }

      return rValue;
   }

   protected void interruptTask() {
   }

   protected final boolean wasInterrupted() {
      Object localValue = this.value();
      return localValue instanceof AbstractFuture.Cancellation && ((AbstractFuture.Cancellation)localValue).wasInterrupted;
   }

   @Override
   public void addListener(Runnable listener, Executor executor) {
      Preconditions.checkNotNull(listener, "Runnable was null.");
      Preconditions.checkNotNull(executor, "Executor was null.");
      if (!this.isDone()) {
         AbstractFuture.Listener oldHead = this.listeners();
         if (oldHead != AbstractFuture.Listener.TOMBSTONE) {
            AbstractFuture.Listener newNode = new AbstractFuture.Listener(listener, executor);

            do {
               newNode.next = oldHead;
               if (this.casListeners(oldHead, newNode)) {
                  return;
               }

               oldHead = this.listeners();
            } while (oldHead != AbstractFuture.Listener.TOMBSTONE);
         }
      }

      executeListener(listener, executor);
   }

   @CanIgnoreReturnValue
   protected boolean set(@ParametricNullness V value) {
      Object valueToSet = value == null ? NULL : value;
      if (casValue(this, null, valueToSet)) {
         complete(this, false);
         return true;
      } else {
         return false;
      }
   }

   @CanIgnoreReturnValue
   protected boolean setException(Throwable throwable) {
      Object valueToSet = new AbstractFuture.Failure(Preconditions.checkNotNull(throwable));
      if (casValue(this, null, valueToSet)) {
         complete(this, false);
         return true;
      } else {
         return false;
      }
   }

   @CanIgnoreReturnValue
   protected boolean setFuture(ListenableFuture<? extends V> future) {
      Preconditions.checkNotNull(future);
      Object localValue = this.value();
      if (localValue == null) {
         if (future.isDone()) {
            Object value = getFutureValue(future);
            if (casValue(this, null, value)) {
               complete(this, false);
               return true;
            }

            return false;
         }

         AbstractFuture.DelegatingToFuture<V> valueToSet = new AbstractFuture.DelegatingToFuture<>(this, future);
         if (casValue(this, null, valueToSet)) {
            try {
               future.addListener(valueToSet, DirectExecutor.INSTANCE);
            } catch (Throwable var8) {
               Throwable t = var8;

               AbstractFuture.Failure failure;
               try {
                  failure = new AbstractFuture.Failure(t);
               } catch (Exception | Error oomMostLikely) {
                  failure = AbstractFuture.Failure.FALLBACK_INSTANCE;
               }

               boolean oomMostLikely = casValue(this, valueToSet, failure);
            }

            return true;
         }

         localValue = this.value();
      }

      if (localValue instanceof AbstractFuture.Cancellation) {
         future.cancel(((AbstractFuture.Cancellation)localValue).wasInterrupted);
      }

      return false;
   }

   private static Object getFutureValue(ListenableFuture<?> future) {
      if (future instanceof AbstractFuture.Trusted) {
         Object v = ((AbstractFuture)future).value();
         if (v instanceof AbstractFuture.Cancellation) {
            AbstractFuture.Cancellation c = (AbstractFuture.Cancellation)v;
            if (c.wasInterrupted) {
               v = c.cause != null ? new AbstractFuture.Cancellation(false, c.cause) : AbstractFuture.Cancellation.CAUSELESS_CANCELLED;
            }
         }

         return Objects.requireNonNull(v);
      } else {
         if (future instanceof InternalFutureFailureAccess) {
            Throwable throwable = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)future);
            if (throwable != null) {
               return new AbstractFuture.Failure(throwable);
            }
         }

         boolean wasCancelled = future.isCancelled();
         if (!GENERATE_CANCELLATION_CAUSES & wasCancelled) {
            return Objects.requireNonNull(AbstractFuture.Cancellation.CAUSELESS_CANCELLED);
         }

         try {
            Object v = getUninterruptibly((Future<V>)future);
            if (wasCancelled) {
               return new AbstractFuture.Cancellation(
                  false, new IllegalArgumentException("get() did not throw CancellationException, despite reporting isCancelled() == true: " + future)
               );
            } else {
               return v == null ? NULL : v;
            }
         } catch (ExecutionException exception) {
            return wasCancelled
               ? new AbstractFuture.Cancellation(
                  false,
                  new IllegalArgumentException("get() did not throw CancellationException, despite reporting isCancelled() == true: " + future, exception)
               )
               : new AbstractFuture.Failure(exception.getCause());
         } catch (CancellationException cancellation) {
            return !wasCancelled
               ? new AbstractFuture.Failure(
                  new IllegalArgumentException("get() threw CancellationException, despite reporting isCancelled() == false: " + future, cancellation)
               )
               : new AbstractFuture.Cancellation(false, cancellation);
         } catch (Exception | Error t) {
            return new AbstractFuture.Failure(t);
         }
      }
   }

   @ParametricNullness
   private static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               return future.get();
            } catch (InterruptedException e) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Platform.interruptCurrentThread();
         }
      }
   }

   private static void complete(AbstractFuture<?> param, boolean callInterruptTask) {
      AbstractFuture<?> future = param;
      AbstractFuture.Listener next = null;

      label27:
      while (true) {
         future.releaseWaiters();
         if (callInterruptTask) {
            future.interruptTask();
            callInterruptTask = false;
         }

         future.afterDone();
         next = future.clearListeners(next);
         AbstractFuture<?> var8 = null;

         while (next != null) {
            AbstractFuture.Listener curr = next;
            next = next.next;
            Runnable task = Objects.requireNonNull(curr.task);
            if (task instanceof AbstractFuture.DelegatingToFuture) {
               AbstractFuture.DelegatingToFuture<?> setFuture = (AbstractFuture.DelegatingToFuture<?>)task;
               future = setFuture.owner;
               if (future.value() == setFuture) {
                  Object valueToSet = getFutureValue(setFuture.future);
                  if (casValue(future, setFuture, valueToSet)) {
                     continue label27;
                  }
               }
            } else {
               executeListener(task, Objects.requireNonNull(curr.executor));
            }
         }

         return;
      }
   }

   @ForOverride
   protected void afterDone() {
   }

   @Override
   protected final @Nullable Throwable tryInternalFastPathGetFailure() {
      if (this instanceof AbstractFuture.Trusted) {
         Object localValue = this.value();
         if (localValue instanceof AbstractFuture.Failure) {
            return ((AbstractFuture.Failure)localValue).exception;
         }
      }

      return null;
   }

   final void maybePropagateCancellationTo(@Nullable Future<?> related) {
      if (related != null & this.isCancelled()) {
         related.cancel(this.wasInterrupted());
      }
   }

   private AbstractFuture.@Nullable Listener clearListeners(AbstractFuture.@Nullable Listener onto) {
      AbstractFuture.Listener head = this.gasListeners(AbstractFuture.Listener.TOMBSTONE);
      AbstractFuture.Listener reversedList = onto;

      while (head != null) {
         AbstractFuture.Listener tmp = head;
         head = head.next;
         tmp.next = reversedList;
         reversedList = tmp;
      }

      return reversedList;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      if (this.getClass().getName().startsWith("com.dfsek.terra.lib.google.common.util.concurrent.")) {
         builder.append(this.getClass().getSimpleName());
      } else {
         builder.append(this.getClass().getName());
      }

      builder.append('@').append(Integer.toHexString(System.identityHashCode(this))).append("[status=");
      if (this.isCancelled()) {
         builder.append("CANCELLED");
      } else if (this.isDone()) {
         this.addDoneString(builder);
      } else {
         this.addPendingString(builder);
      }

      return builder.append("]").toString();
   }

   protected @Nullable String pendingToString() {
      return this instanceof ScheduledFuture ? "remaining delay=[" + ((ScheduledFuture)this).getDelay(TimeUnit.MILLISECONDS) + " ms]" : null;
   }

   private void addPendingString(StringBuilder builder) {
      int truncateLength = builder.length();
      builder.append("PENDING");
      Object localValue = this.value();
      if (localValue instanceof AbstractFuture.DelegatingToFuture) {
         builder.append(", setFuture=[");
         this.appendUserObject(builder, ((AbstractFuture.DelegatingToFuture)localValue).future);
         builder.append("]");
      } else {
         String pendingDescription;
         try {
            pendingDescription = Strings.emptyToNull(this.pendingToString());
         } catch (Throwable e) {
            Platform.rethrowIfErrorOtherThanStackOverflow(e);
            pendingDescription = "Exception thrown from implementation: " + e.getClass();
         }

         if (pendingDescription != null) {
            builder.append(", info=[").append(pendingDescription).append("]");
         }
      }

      if (this.isDone()) {
         builder.delete(truncateLength, builder.length());
         this.addDoneString(builder);
      }
   }

   private void addDoneString(StringBuilder builder) {
      try {
         V value = getUninterruptibly(this);
         builder.append("SUCCESS, result=[");
         this.appendResultObject(builder, value);
         builder.append("]");
      } catch (ExecutionException e) {
         builder.append("FAILURE, cause=[").append(e.getCause()).append("]");
      } catch (CancellationException e) {
         builder.append("CANCELLED");
      } catch (Exception e) {
         builder.append("UNKNOWN, cause=[").append(e.getClass()).append(" thrown from get()]");
      }
   }

   private void appendResultObject(StringBuilder builder, @Nullable Object o) {
      if (o == null) {
         builder.append("null");
      } else if (o == this) {
         builder.append("this future");
      } else {
         builder.append(o.getClass().getName()).append("@").append(Integer.toHexString(System.identityHashCode(o)));
      }
   }

   private void appendUserObject(StringBuilder builder, @Nullable Object o) {
      try {
         if (o == this) {
            builder.append("this future");
         } else {
            builder.append(o);
         }
      } catch (Throwable e) {
         Platform.rethrowIfErrorOtherThanStackOverflow(e);
         builder.append("Exception thrown from implementation: ").append(e.getClass());
      }
   }

   private static void executeListener(Runnable runnable, Executor executor) {
      try {
         executor.execute(runnable);
      } catch (Exception e) {
         log.get().log(java.util.logging.Level.SEVERE, "RuntimeException while executing runnable " + runnable + " with executor " + executor, e);
      }
   }

   private static CancellationException cancellationExceptionWithCause(String message, @Nullable Throwable cause) {
      CancellationException exception = new CancellationException(message);
      exception.initCause(cause);
      return exception;
   }

   private static final class Cancellation {
      static final AbstractFuture.@Nullable Cancellation CAUSELESS_INTERRUPTED;
      static final AbstractFuture.@Nullable Cancellation CAUSELESS_CANCELLED;
      final boolean wasInterrupted;
      final @Nullable Throwable cause;

      Cancellation(boolean wasInterrupted, @Nullable Throwable cause) {
         this.wasInterrupted = wasInterrupted;
         this.cause = cause;
      }

      static {
         if (AbstractFutureState.GENERATE_CANCELLATION_CAUSES) {
            CAUSELESS_CANCELLED = null;
            CAUSELESS_INTERRUPTED = null;
         } else {
            CAUSELESS_CANCELLED = new AbstractFuture.Cancellation(false, null);
            CAUSELESS_INTERRUPTED = new AbstractFuture.Cancellation(true, null);
         }
      }
   }

   private static final class DelegatingToFuture<V> implements Runnable {
      final AbstractFuture<V> owner;
      final ListenableFuture<? extends V> future;

      DelegatingToFuture(AbstractFuture<V> owner, ListenableFuture<? extends V> future) {
         this.owner = owner;
         this.future = future;
      }

      @Override
      public void run() {
         if (this.owner.value() == this) {
            Object valueToSet = AbstractFuture.getFutureValue(this.future);
            if (AbstractFutureState.casValue(this.owner, this, valueToSet)) {
               AbstractFuture.complete(this.owner, false);
            }
         }
      }
   }

   private static final class Failure {
      static final AbstractFuture.Failure FALLBACK_INSTANCE = new AbstractFuture.Failure(new Throwable("Failure occurred while trying to finish a future.") {
         @Override
         public Throwable fillInStackTrace() {
            return this;
         }
      });
      final Throwable exception;

      Failure(Throwable exception) {
         this.exception = Preconditions.checkNotNull(exception);
      }
   }

   static final class Listener {
      static final AbstractFuture.Listener TOMBSTONE = new AbstractFuture.Listener();
      final @Nullable Runnable task;
      final @Nullable Executor executor;
      AbstractFuture.@Nullable Listener next;

      Listener(Runnable task, Executor executor) {
         this.task = task;
         this.executor = executor;
      }

      Listener() {
         this.task = null;
         this.executor = null;
      }
   }

   interface Trusted<V> extends ListenableFuture<V> {
   }

   abstract static class TrustedFuture<V> extends AbstractFuture<V> implements AbstractFuture.Trusted<V> {
      @CanIgnoreReturnValue
      @ParametricNullness
      @Override
      public final V get() throws InterruptedException, ExecutionException {
         return super.get();
      }

      @CanIgnoreReturnValue
      @ParametricNullness
      @Override
      public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
         return super.get(timeout, unit);
      }

      @Override
      public final boolean isDone() {
         return super.isDone();
      }

      @Override
      public final boolean isCancelled() {
         return super.isCancelled();
      }

      @Override
      public final void addListener(Runnable listener, Executor executor) {
         super.addListener(listener, executor);
      }

      @CanIgnoreReturnValue
      @Override
      public final boolean cancel(boolean mayInterruptIfRunning) {
         return super.cancel(mayInterruptIfRunning);
      }
   }
}
