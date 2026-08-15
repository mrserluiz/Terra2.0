package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class Platform {
   static boolean isInstanceOfThrowableClass(@Nullable Throwable t, Class<? extends Throwable> expectedClass) {
      return expectedClass.isInstance(t);
   }

   static void restoreInterruptIfIsInterruptedException(Throwable t) {
      Preconditions.checkNotNull(t);
      if (t instanceof InterruptedException) {
         Thread.currentThread().interrupt();
      }
   }

   static void interruptCurrentThread() {
      Thread.currentThread().interrupt();
   }

   static void rethrowIfErrorOtherThanStackOverflow(Throwable t) {
      Preconditions.checkNotNull(t);
      if (t instanceof Error && !(t instanceof StackOverflowError)) {
         throw (Error)t;
      }
   }

   static <V> V get(AbstractFuture<V> future) throws InterruptedException, ExecutionException {
      return future.blockingGet();
   }

   static <V> V get(AbstractFuture<V> future, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return future.blockingGet(timeout, unit);
   }

   private Platform() {
   }
}
