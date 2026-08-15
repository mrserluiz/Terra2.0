package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.google.j2objc.annotations.ReflectionSupport;
import com.google.j2objc.annotations.ReflectionSupport.Level;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;
import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

@GwtCompatible(emulated = true)
@ReflectionSupport(Level.FULL)
abstract class AbstractFutureState<V> extends InternalFutureFailureAccess implements ListenableFuture<V> {
   static final Object NULL = new Object();
   static final LazyLogger log = new LazyLogger(AbstractFuture.class);
   static final boolean GENERATE_CANCELLATION_CAUSES;
   private static final AbstractFutureState.AtomicHelper ATOMIC_HELPER;
   volatile @Nullable Object valueField;
   volatile AbstractFuture.@Nullable Listener listenersField;
   volatile AbstractFutureState.@Nullable Waiter waitersField;
   private static final long SPIN_THRESHOLD_NANOS = 1000L;

   final boolean casListeners(AbstractFuture.@Nullable Listener expect, AbstractFuture.Listener update) {
      return ATOMIC_HELPER.casListeners(this, expect, update);
   }

   final AbstractFuture.@Nullable Listener gasListeners(AbstractFuture.Listener update) {
      return ATOMIC_HELPER.gasListeners(this, update);
   }

   static boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
      return ATOMIC_HELPER.casValue(future, expect, update);
   }

   final @Nullable Object value() {
      return this.valueField;
   }

   final AbstractFuture.@Nullable Listener listeners() {
      return this.listenersField;
   }

   final void releaseWaiters() {
      AbstractFutureState.Waiter head = this.gasWaiters(AbstractFutureState.Waiter.TOMBSTONE);

      for (AbstractFutureState.Waiter currentWaiter = head; currentWaiter != null; currentWaiter = currentWaiter.next) {
         currentWaiter.unpark();
      }
   }

   @ParametricNullness
   final V blockingGet(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
      long timeoutNanos = unit.toNanos(timeout);
      long remainingNanos = timeoutNanos;
      if (Thread.interrupted()) {
         throw new InterruptedException();
      }

      Object localValue = this.valueField;
      if (localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) {
         return AbstractFuture.getDoneValue(localValue);
      }

      long endNanos = remainingNanos > 0L ? System.nanoTime() + remainingNanos : 0L;
      if (remainingNanos >= 1000L) {
         AbstractFutureState.Waiter oldHead = this.waitersField;
         if (oldHead == AbstractFutureState.Waiter.TOMBSTONE) {
            return AbstractFuture.getDoneValue(Objects.requireNonNull(this.valueField));
         }

         AbstractFutureState.Waiter node = new AbstractFutureState.Waiter();

         while (true) {
            node.setNext(oldHead);
            if (this.casWaiters(oldHead, node)) {
               do {
                  OverflowAvoidingLockSupport.parkNanos(this, remainingNanos);
                  if (Thread.interrupted()) {
                     this.removeWaiter(node);
                     throw new InterruptedException();
                  }

                  localValue = this.valueField;
                  if (localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) {
                     return AbstractFuture.getDoneValue(localValue);
                  }

                  remainingNanos = endNanos - System.nanoTime();
               } while (remainingNanos >= 1000L);

               this.removeWaiter(node);
               break;
            }

            oldHead = this.waitersField;
            if (oldHead == AbstractFutureState.Waiter.TOMBSTONE) {
               return AbstractFuture.getDoneValue(Objects.requireNonNull(this.valueField));
            }
         }
      }

      while (remainingNanos > 0L) {
         localValue = this.valueField;
         if (localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) {
            return AbstractFuture.getDoneValue(localValue);
         }

         if (Thread.interrupted()) {
            throw new InterruptedException();
         }

         remainingNanos = endNanos - System.nanoTime();
      }

      String futureToString = this.toString();
      String unitString = unit.toString().toLowerCase(Locale.ROOT);
      String message = "Waited " + timeout + " " + unit.toString().toLowerCase(Locale.ROOT);
      if (remainingNanos + 1000L < 0L) {
         message = message + " (plus ";
         long overWaitNanos = -remainingNanos;
         long overWaitUnits = unit.convert(overWaitNanos, TimeUnit.NANOSECONDS);
         long overWaitLeftoverNanos = overWaitNanos - unit.toNanos(overWaitUnits);
         boolean shouldShowExtraNanos = overWaitUnits == 0L || overWaitLeftoverNanos > 1000L;
         if (overWaitUnits > 0L) {
            message = message + overWaitUnits + " " + unitString;
            if (shouldShowExtraNanos) {
               message = message + ",";
            }

            message = message + " ";
         }

         if (shouldShowExtraNanos) {
            message = message + overWaitLeftoverNanos + " nanoseconds ";
         }

         message = message + "delay)";
      }

      if (this.isDone()) {
         throw new TimeoutException(message + " but future completed as timeout expired");
      } else {
         throw new TimeoutException(message + " for " + futureToString);
      }
   }

   @ParametricNullness
   final V blockingGet() throws InterruptedException, ExecutionException {
      if (Thread.interrupted()) {
         throw new InterruptedException();
      }

      Object localValue = this.valueField;
      if (localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) {
         return AbstractFuture.getDoneValue(localValue);
      }

      AbstractFutureState.Waiter oldHead = this.waitersField;
      if (oldHead != AbstractFutureState.Waiter.TOMBSTONE) {
         AbstractFutureState.Waiter node = new AbstractFutureState.Waiter();

         do {
            node.setNext(oldHead);
            if (this.casWaiters(oldHead, node)) {
               do {
                  LockSupport.park(this);
                  if (Thread.interrupted()) {
                     this.removeWaiter(node);
                     throw new InterruptedException();
                  }

                  localValue = this.valueField;
               } while (!(localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)));

               return AbstractFuture.getDoneValue(localValue);
            }

            oldHead = this.waitersField;
         } while (oldHead != AbstractFutureState.Waiter.TOMBSTONE);
      }

      return AbstractFuture.getDoneValue(Objects.requireNonNull(this.valueField));
   }

   private static void putThread(AbstractFutureState.Waiter waiter, Thread newValue) {
      ATOMIC_HELPER.putThread(waiter, newValue);
   }

   private static void putNext(AbstractFutureState.Waiter waiter, AbstractFutureState.@Nullable Waiter newValue) {
      ATOMIC_HELPER.putNext(waiter, newValue);
   }

   private boolean casWaiters(AbstractFutureState.@Nullable Waiter expect, AbstractFutureState.@Nullable Waiter update) {
      return ATOMIC_HELPER.casWaiters(this, expect, update);
   }

   private final AbstractFutureState.@Nullable Waiter gasWaiters(AbstractFutureState.Waiter update) {
      return ATOMIC_HELPER.gasWaiters(this, update);
   }

   private void removeWaiter(AbstractFutureState.Waiter node) {
      node.thread = null;

      label28:
      while (true) {
         AbstractFutureState.Waiter pred = null;
         AbstractFutureState.Waiter curr = this.waitersField;
         if (curr == AbstractFutureState.Waiter.TOMBSTONE) {
            return;
         }

         while (curr != null) {
            AbstractFutureState.Waiter succ = curr.next;
            if (curr.thread != null) {
               pred = curr;
            } else if (pred != null) {
               pred.next = succ;
               if (pred.thread == null) {
                  continue label28;
               }
            } else if (!this.casWaiters(curr, succ)) {
               continue label28;
            }

            curr = succ;
         }

         return;
      }
   }

   @VisibleForTesting
   static String atomicHelperTypeForTest() {
      return ATOMIC_HELPER.atomicHelperTypeForTest();
   }

   static {
      boolean generateCancellationCauses;
      try {
         generateCancellationCauses = Boolean.parseBoolean(System.getProperty("guava.concurrent.generate_cancellation_cause", "false"));
      } catch (SecurityException e) {
         generateCancellationCauses = false;
      }

      GENERATE_CANCELLATION_CAUSES = generateCancellationCauses;
      Throwable thrownUnsafeFailure = null;
      Throwable thrownAtomicReferenceFieldUpdaterFailure = null;
      AbstractFutureState.AtomicHelper helper = AbstractFutureState.VarHandleAtomicHelperMaker.INSTANCE.tryMakeVarHandleAtomicHelper();
      if (helper == null) {
         try {
            helper = new AbstractFutureState.UnsafeAtomicHelper();
         } catch (Exception | Error unsafeFailure) {
            thrownUnsafeFailure = unsafeFailure;

            try {
               helper = new AbstractFutureState.AtomicReferenceFieldUpdaterAtomicHelper();
            } catch (Exception | Error atomicReferenceFieldUpdaterFailure) {
               thrownAtomicReferenceFieldUpdaterFailure = atomicReferenceFieldUpdaterFailure;
               helper = new AbstractFutureState.SynchronizedHelper();
            }
         }
      }

      ATOMIC_HELPER = helper;
      Class<?> ensureLoaded = LockSupport.class;
      if (thrownAtomicReferenceFieldUpdaterFailure != null) {
         log.get().log(java.util.logging.Level.SEVERE, "UnsafeAtomicHelper is broken!", thrownUnsafeFailure);
         log.get().log(java.util.logging.Level.SEVERE, "AtomicReferenceFieldUpdaterAtomicHelper is broken!", thrownAtomicReferenceFieldUpdaterFailure);
      }
   }

   private abstract static class AtomicHelper {
      private AtomicHelper() {
      }

      abstract void putThread(AbstractFutureState.Waiter waiter, Thread newValue);

      abstract void putNext(AbstractFutureState.Waiter waiter, AbstractFutureState.@Nullable Waiter newValue);

      abstract boolean casWaiters(AbstractFutureState<?> future, AbstractFutureState.@Nullable Waiter expect, AbstractFutureState.@Nullable Waiter update);

      abstract boolean casListeners(AbstractFutureState<?> future, AbstractFuture.@Nullable Listener expect, AbstractFuture.Listener update);

      abstract AbstractFutureState.@Nullable Waiter gasWaiters(AbstractFutureState<?> future, AbstractFutureState.Waiter update);

      abstract AbstractFuture.@Nullable Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update);

      abstract boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update);

      abstract String atomicHelperTypeForTest();
   }

   private static final class AtomicReferenceFieldUpdaterAtomicHelper extends AbstractFutureState.AtomicHelper {
      private static final AtomicReferenceFieldUpdater<AbstractFutureState.Waiter, @Nullable Thread> waiterThreadUpdater = AtomicReferenceFieldUpdater.newUpdater(
         AbstractFutureState.Waiter.class, Thread.class, "thread"
      );
      private static final AtomicReferenceFieldUpdater<AbstractFutureState.Waiter, AbstractFutureState.@Nullable Waiter> waiterNextUpdater = AtomicReferenceFieldUpdater.newUpdater(
         AbstractFutureState.Waiter.class, AbstractFutureState.Waiter.class, "next"
      );
      private static final AtomicReferenceFieldUpdater<? super AbstractFutureState<?>, AbstractFutureState.@Nullable Waiter> waitersUpdater = AtomicReferenceFieldUpdater.newUpdater(
         AbstractFutureState.class, AbstractFutureState.Waiter.class, "waitersField"
      );
      private static final AtomicReferenceFieldUpdater<? super AbstractFutureState<?>, AbstractFuture.@Nullable Listener> listenersUpdater = AtomicReferenceFieldUpdater.newUpdater(
         AbstractFutureState.class, AbstractFuture.Listener.class, "listenersField"
      );
      private static final AtomicReferenceFieldUpdater<? super AbstractFutureState<?>, @Nullable Object> valueUpdater = AtomicReferenceFieldUpdater.newUpdater(
         AbstractFutureState.class, Object.class, "valueField"
      );

      private AtomicReferenceFieldUpdaterAtomicHelper() {
      }

      @Override
      void putThread(AbstractFutureState.Waiter waiter, Thread newValue) {
         waiterThreadUpdater.lazySet(waiter, newValue);
      }

      @Override
      void putNext(AbstractFutureState.Waiter waiter, AbstractFutureState.@Nullable Waiter newValue) {
         waiterNextUpdater.lazySet(waiter, newValue);
      }

      @Override
      boolean casWaiters(AbstractFutureState<?> future, AbstractFutureState.@Nullable Waiter expect, AbstractFutureState.@Nullable Waiter update) {
         return waitersUpdater.compareAndSet(future, expect, update);
      }

      @Override
      boolean casListeners(AbstractFutureState<?> future, AbstractFuture.@Nullable Listener expect, AbstractFuture.Listener update) {
         return listenersUpdater.compareAndSet(future, expect, update);
      }

      @Override
      AbstractFuture.@Nullable Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
         return listenersUpdater.getAndSet(future, update);
      }

      @Override
      AbstractFutureState.@Nullable Waiter gasWaiters(AbstractFutureState<?> future, AbstractFutureState.Waiter update) {
         return waitersUpdater.getAndSet(future, update);
      }

      @Override
      boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
         return valueUpdater.compareAndSet(future, expect, update);
      }

      @Override
      String atomicHelperTypeForTest() {
         return "AtomicReferenceFieldUpdaterAtomicHelper";
      }
   }

   private static final class SynchronizedHelper extends AbstractFutureState.AtomicHelper {
      private SynchronizedHelper() {
      }

      @Override
      void putThread(AbstractFutureState.Waiter waiter, Thread newValue) {
         waiter.thread = newValue;
      }

      @Override
      void putNext(AbstractFutureState.Waiter waiter, AbstractFutureState.@Nullable Waiter newValue) {
         waiter.next = newValue;
      }

      @Override
      boolean casWaiters(AbstractFutureState<?> future, AbstractFutureState.@Nullable Waiter expect, AbstractFutureState.@Nullable Waiter update) {
         synchronized (future) {
            if (future.waitersField == expect) {
               future.waitersField = update;
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      boolean casListeners(AbstractFutureState<?> future, AbstractFuture.@Nullable Listener expect, AbstractFuture.Listener update) {
         synchronized (future) {
            if (future.listenersField == expect) {
               future.listenersField = update;
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      AbstractFuture.@Nullable Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
         synchronized (future) {
            AbstractFuture.Listener old = future.listenersField;
            if (old != update) {
               future.listenersField = update;
            }

            return old;
         }
      }

      @Override
      AbstractFutureState.@Nullable Waiter gasWaiters(AbstractFutureState<?> future, AbstractFutureState.Waiter update) {
         synchronized (future) {
            AbstractFutureState.Waiter old = future.waitersField;
            if (old != update) {
               future.waitersField = update;
            }

            return old;
         }
      }

      @Override
      boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
         synchronized (future) {
            if (future.valueField == expect) {
               future.valueField = update;
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      String atomicHelperTypeForTest() {
         return "SynchronizedHelper";
      }
   }

   private static final class UnsafeAtomicHelper extends AbstractFutureState.AtomicHelper {
      static final Unsafe UNSAFE;
      static final long LISTENERS_OFFSET;
      static final long WAITERS_OFFSET;
      static final long VALUE_OFFSET;
      static final long WAITER_THREAD_OFFSET;
      static final long WAITER_NEXT_OFFSET;

      private UnsafeAtomicHelper() {
      }

      @Override
      void putThread(AbstractFutureState.Waiter waiter, Thread newValue) {
         UNSAFE.putObject(waiter, WAITER_THREAD_OFFSET, newValue);
      }

      @Override
      void putNext(AbstractFutureState.Waiter waiter, AbstractFutureState.@Nullable Waiter newValue) {
         UNSAFE.putObject(waiter, WAITER_NEXT_OFFSET, newValue);
      }

      @Override
      boolean casWaiters(AbstractFutureState<?> future, AbstractFutureState.@Nullable Waiter expect, AbstractFutureState.@Nullable Waiter update) {
         return UNSAFE.compareAndSwapObject(future, WAITERS_OFFSET, expect, update);
      }

      @Override
      boolean casListeners(AbstractFutureState<?> future, AbstractFuture.@Nullable Listener expect, AbstractFuture.Listener update) {
         return UNSAFE.compareAndSwapObject(future, LISTENERS_OFFSET, expect, update);
      }

      @Override
      AbstractFuture.@Nullable Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
         return (AbstractFuture.Listener)UNSAFE.getAndSetObject(future, LISTENERS_OFFSET, update);
      }

      @Override
      AbstractFutureState.@Nullable Waiter gasWaiters(AbstractFutureState<?> future, AbstractFutureState.Waiter update) {
         return (AbstractFutureState.Waiter)UNSAFE.getAndSetObject(future, WAITERS_OFFSET, update);
      }

      @Override
      boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
         return UNSAFE.compareAndSwapObject(future, VALUE_OFFSET, expect, update);
      }

      @Override
      String atomicHelperTypeForTest() {
         return "UnsafeAtomicHelper";
      }

      static {
         Unsafe unsafe = null;

         try {
            unsafe = Unsafe.getUnsafe();
         } catch (SecurityException tryReflectionInstead) {
            try {
               unsafe = AccessController.doPrivileged(() -> {
                  Class<Unsafe> k = Unsafe.class;

                  for (Field f : k.getDeclaredFields()) {
                     f.setAccessible(true);
                     Object x = f.get(null);
                     if (k.isInstance(x)) {
                        return k.cast(x);
                     }
                  }

                  throw new NoSuchFieldError("the Unsafe");
               });
            } catch (PrivilegedActionException e) {
               throw new RuntimeException("Could not initialize intrinsics", e.getCause());
            }
         }

         try {
            Class<?> abstractFutureState = AbstractFutureState.class;
            WAITERS_OFFSET = unsafe.objectFieldOffset(abstractFutureState.getDeclaredField("waitersField"));
            LISTENERS_OFFSET = unsafe.objectFieldOffset(abstractFutureState.getDeclaredField("listenersField"));
            VALUE_OFFSET = unsafe.objectFieldOffset(abstractFutureState.getDeclaredField("valueField"));
            WAITER_THREAD_OFFSET = unsafe.objectFieldOffset(AbstractFutureState.Waiter.class.getDeclaredField("thread"));
            WAITER_NEXT_OFFSET = unsafe.objectFieldOffset(AbstractFutureState.Waiter.class.getDeclaredField("next"));
            UNSAFE = unsafe;
         } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
         }
      }
   }

   @IgnoreJRERequirement
   private static final class VarHandleAtomicHelper extends AbstractFutureState.AtomicHelper {
      static final VarHandle waiterThreadUpdater;
      static final VarHandle waiterNextUpdater;
      static final VarHandle waitersUpdater;
      static final VarHandle listenersUpdater;
      static final VarHandle valueUpdater;

      private VarHandleAtomicHelper() {
      }

      @Override
      void putThread(AbstractFutureState.Waiter waiter, Thread newValue) {
         waiterThreadUpdater.setRelease((AbstractFutureState.Waiter)waiter, (Thread)newValue);
      }

      @Override
      void putNext(AbstractFutureState.Waiter waiter, AbstractFutureState.@Nullable Waiter newValue) {
         waiterNextUpdater.setRelease((AbstractFutureState.Waiter)waiter, (AbstractFutureState.Waiter)newValue);
      }

      @Override
      boolean casWaiters(AbstractFutureState<?> future, AbstractFutureState.@Nullable Waiter expect, AbstractFutureState.@Nullable Waiter update) {
         return waitersUpdater.compareAndSet((AbstractFutureState)future, (AbstractFutureState.Waiter)expect, (AbstractFutureState.Waiter)update);
      }

      @Override
      boolean casListeners(AbstractFutureState<?> future, AbstractFuture.@Nullable Listener expect, AbstractFuture.Listener update) {
         return listenersUpdater.compareAndSet((AbstractFutureState)future, (AbstractFuture.Listener)expect, (AbstractFuture.Listener)update);
      }

      @Override
      AbstractFuture.@Nullable Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
         return (AbstractFuture.Listener)listenersUpdater.getAndSet((AbstractFutureState)future, (AbstractFuture.Listener)update);
      }

      @Override
      AbstractFutureState.@Nullable Waiter gasWaiters(AbstractFutureState<?> future, AbstractFutureState.Waiter update) {
         return (AbstractFutureState.Waiter)waitersUpdater.getAndSet((AbstractFutureState)future, (AbstractFutureState.Waiter)update);
      }

      @Override
      boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
         return valueUpdater.compareAndSet((AbstractFutureState)future, (Object)expect, (Object)update);
      }

      private static LinkageError newLinkageError(Throwable cause) {
         return new LinkageError(cause.toString(), cause);
      }

      @Override
      String atomicHelperTypeForTest() {
         return "VarHandleAtomicHelper";
      }

      static {
         Lookup lookup = MethodHandles.lookup();

         try {
            waiterThreadUpdater = lookup.findVarHandle(AbstractFutureState.Waiter.class, "thread", Thread.class);
            waiterNextUpdater = lookup.findVarHandle(AbstractFutureState.Waiter.class, "next", AbstractFutureState.Waiter.class);
            waitersUpdater = lookup.findVarHandle(AbstractFutureState.class, "waitersField", AbstractFutureState.Waiter.class);
            listenersUpdater = lookup.findVarHandle(AbstractFutureState.class, "listenersField", AbstractFuture.Listener.class);
            valueUpdater = lookup.findVarHandle(AbstractFutureState.class, "valueField", Object.class);
         } catch (ReflectiveOperationException e) {
            throw newLinkageError(e);
         }
      }
   }

   private enum VarHandleAtomicHelperMaker {
      INSTANCE {
         @Override
         AbstractFutureState.@Nullable AtomicHelper tryMakeVarHandleAtomicHelper() {
            try {
               Class.forName("java.lang.invoke.VarHandle");
            } catch (ClassNotFoundException beforeJava9) {
               return null;
            }

            return new AbstractFutureState.VarHandleAtomicHelper();
         }
      };

      VarHandleAtomicHelperMaker() {
      }

      AbstractFutureState.@Nullable AtomicHelper tryMakeVarHandleAtomicHelper() {
         return null;
      }
   }

   static final class Waiter {
      static final AbstractFutureState.Waiter TOMBSTONE = new AbstractFutureState.Waiter(false);
      volatile @Nullable Thread thread;
      volatile AbstractFutureState.@Nullable Waiter next;

      Waiter(boolean unused) {
      }

      Waiter() {
         AbstractFutureState.putThread(this, Thread.currentThread());
      }

      void setNext(AbstractFutureState.@Nullable Waiter next) {
         AbstractFutureState.putNext(this, next);
      }

      void unpark() {
         Thread w = this.thread;
         if (w != null) {
            this.thread = null;
            LockSupport.unpark(w);
         }
      }
   }
}
