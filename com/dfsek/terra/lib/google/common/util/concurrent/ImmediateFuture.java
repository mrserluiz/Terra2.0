package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@GwtCompatible
class ImmediateFuture<V> implements ListenableFuture<V> {
   static final ListenableFuture<?> NULL = new ImmediateFuture(null);
   private static final LazyLogger log = new LazyLogger(ImmediateFuture.class);
   @ParametricNullness
   private final V value;

   ImmediateFuture(@ParametricNullness V value) {
      this.value = value;
   }

   @Override
   public void addListener(Runnable listener, Executor executor) {
      Preconditions.checkNotNull(listener, "Runnable was null.");
      Preconditions.checkNotNull(executor, "Executor was null.");

      try {
         executor.execute(listener);
      } catch (Exception e) {
         log.get().log(Level.SEVERE, "RuntimeException while executing runnable " + listener + " with executor " + executor, e);
      }
   }

   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
   }

   @ParametricNullness
   @Override
   public V get() {
      return this.value;
   }

   @ParametricNullness
   @Override
   public V get(long timeout, TimeUnit unit) throws ExecutionException {
      Preconditions.checkNotNull(unit);
      return this.get();
   }

   @Override
   public boolean isCancelled() {
      return false;
   }

   @Override
   public boolean isDone() {
      return true;
   }

   @Override
   public String toString() {
      return super.toString() + "[status=SUCCESS, result=[" + this.value + "]]";
   }

   static final class ImmediateCancelledFuture<V> extends AbstractFuture.TrustedFuture<V> {
      static final ImmediateFuture.@Nullable ImmediateCancelledFuture<Object> INSTANCE = AbstractFuture.GENERATE_CANCELLATION_CAUSES
         ? null
         : new ImmediateFuture.ImmediateCancelledFuture<>();

      ImmediateCancelledFuture() {
         this.cancel(false);
      }
   }

   static final class ImmediateFailedFuture<V> extends AbstractFuture.TrustedFuture<V> {
      ImmediateFailedFuture(Throwable thrown) {
         this.setException(thrown);
      }
   }
}
