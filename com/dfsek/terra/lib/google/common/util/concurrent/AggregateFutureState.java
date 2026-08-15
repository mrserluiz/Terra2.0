package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.collect.Sets;
import com.google.j2objc.annotations.ReflectionSupport;
import com.google.j2objc.annotations.ReflectionSupport.Level;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
@ReflectionSupport(Level.FULL)
abstract class AggregateFutureState<OutputT> extends AbstractFuture.TrustedFuture<OutputT> {
   volatile @Nullable Set<Throwable> seenExceptionsField = null;
   volatile int remainingField;
   private static final AggregateFutureState.AtomicHelper ATOMIC_HELPER;
   private static final LazyLogger log = new LazyLogger(AggregateFutureState.class);

   AggregateFutureState(int remainingFutures) {
      this.remainingField = remainingFutures;
   }

   final Set<Throwable> getOrInitSeenExceptions() {
      Set<Throwable> seenExceptionsLocal = this.seenExceptionsField;
      if (seenExceptionsLocal == null) {
         seenExceptionsLocal = Sets.newConcurrentHashSet();
         this.addInitialException(seenExceptionsLocal);
         ATOMIC_HELPER.compareAndSetSeenExceptions(this, null, seenExceptionsLocal);
         seenExceptionsLocal = Objects.requireNonNull(this.seenExceptionsField);
      }

      return seenExceptionsLocal;
   }

   abstract void addInitialException(Set<Throwable> seen);

   final int decrementRemainingAndGet() {
      return ATOMIC_HELPER.decrementAndGetRemainingCount(this);
   }

   final void clearSeenExceptions() {
      this.seenExceptionsField = null;
   }

   @VisibleForTesting
   static String atomicHelperTypeForTest() {
      return ATOMIC_HELPER.atomicHelperTypeForTest();
   }

   static {
      Throwable thrownReflectionFailure = null;

      AggregateFutureState.AtomicHelper helper;
      try {
         helper = new AggregateFutureState.SafeAtomicHelper();
      } catch (Throwable reflectionFailure) {
         thrownReflectionFailure = reflectionFailure;
         helper = new AggregateFutureState.SynchronizedAtomicHelper();
      }

      ATOMIC_HELPER = helper;
      if (thrownReflectionFailure != null) {
         log.get().log(java.util.logging.Level.SEVERE, "SafeAtomicHelper is broken!", thrownReflectionFailure);
      }
   }

   private abstract static class AtomicHelper {
      private AtomicHelper() {
      }

      abstract void compareAndSetSeenExceptions(AggregateFutureState<?> state, @Nullable Set<Throwable> expect, Set<Throwable> update);

      abstract int decrementAndGetRemainingCount(AggregateFutureState<?> state);

      abstract String atomicHelperTypeForTest();
   }

   private static final class SafeAtomicHelper extends AggregateFutureState.AtomicHelper {
      private static final AtomicReferenceFieldUpdater<? super AggregateFutureState<?>, ? super @Nullable Set<Throwable>> seenExceptionsUpdater = AtomicReferenceFieldUpdater.newUpdater(
         AggregateFutureState.class, Set.class, "seenExceptionsField"
      );
      private static final AtomicIntegerFieldUpdater<? super AggregateFutureState<?>> remainingCountUpdater = AtomicIntegerFieldUpdater.newUpdater(
         AggregateFutureState.class, "remainingField"
      );

      private SafeAtomicHelper() {
      }

      @Override
      void compareAndSetSeenExceptions(AggregateFutureState<?> state, @Nullable Set<Throwable> expect, Set<Throwable> update) {
         seenExceptionsUpdater.compareAndSet(state, expect, update);
      }

      @Override
      int decrementAndGetRemainingCount(AggregateFutureState<?> state) {
         return remainingCountUpdater.decrementAndGet(state);
      }

      @Override
      String atomicHelperTypeForTest() {
         return "SafeAtomicHelper";
      }
   }

   private static final class SynchronizedAtomicHelper extends AggregateFutureState.AtomicHelper {
      private SynchronizedAtomicHelper() {
      }

      @Override
      void compareAndSetSeenExceptions(AggregateFutureState<?> state, @Nullable Set<Throwable> expect, Set<Throwable> update) {
         synchronized (state) {
            if (state.seenExceptionsField == expect) {
               state.seenExceptionsField = update;
            }
         }
      }

      @Override
      int decrementAndGetRemainingCount(AggregateFutureState<?> state) {
         synchronized (state) {
            return --state.remainingField;
         }
      }

      @Override
      String atomicHelperTypeForTest() {
         return "SynchronizedAtomicHelper";
      }
   }
}
