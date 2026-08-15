package com.dfsek.terra.lib.google.common.base.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

public class Finalizer implements Runnable {
   private static final Logger logger = Logger.getLogger(Finalizer.class.getName());
   private static final String FINALIZABLE_REFERENCE = "com.dfsek.terra.lib.google.common.base.FinalizableReference";
   private final WeakReference<Class<?>> finalizableReferenceClassReference;
   private final PhantomReference<Object> frqReference;
   private final ReferenceQueue<Object> queue;
   private static final @Nullable Constructor<Thread> bigThreadConstructor = getBigThreadConstructor();
   private static final @Nullable Field inheritableThreadLocals = bigThreadConstructor == null ? getInheritableThreadLocalsField() : null;

   public static void startFinalizer(Class<?> finalizableReferenceClass, ReferenceQueue<Object> queue, PhantomReference<Object> frqReference) {
      if (!finalizableReferenceClass.getName().equals("com.dfsek.terra.lib.google.common.base.FinalizableReference")) {
         throw new IllegalArgumentException("Expected com.google.common.base.FinalizableReference.");
      }

      Finalizer finalizer = new Finalizer(finalizableReferenceClass, queue, frqReference);
      String threadName = Finalizer.class.getName();
      Thread thread = null;
      if (bigThreadConstructor != null) {
         try {
            boolean inheritThreadLocals = false;
            long defaultStackSize = 0L;
            thread = bigThreadConstructor.newInstance((ThreadGroup)null, finalizer, threadName, defaultStackSize, inheritThreadLocals);
         } catch (Throwable t) {
            logger.log(Level.INFO, "Failed to create a thread without inherited thread-local values", t);
         }
      }

      if (thread == null) {
         thread = new Thread((ThreadGroup)null, finalizer, threadName);
      }

      thread.setDaemon(true);

      try {
         if (inheritableThreadLocals != null) {
            inheritableThreadLocals.set(thread, null);
         }
      } catch (Throwable t) {
         logger.log(Level.INFO, "Failed to clear thread local values inherited by reference finalizer thread.", t);
      }

      thread.start();
   }

   private Finalizer(Class<?> finalizableReferenceClass, ReferenceQueue<Object> queue, PhantomReference<Object> frqReference) {
      this.queue = queue;
      this.finalizableReferenceClassReference = new WeakReference<>(finalizableReferenceClass);
      this.frqReference = frqReference;
   }

   @Override
   public void run() {
      while (true) {
         try {
            if (!this.cleanUp(this.queue.remove())) {
               return;
            }
         } catch (InterruptedException var2) {
         }
      }
   }

   private boolean cleanUp(Reference<?> firstReference) {
      Method finalizeReferentMethod = this.getFinalizeReferentMethod();
      if (finalizeReferentMethod == null) {
         return false;
      }

      if (!this.finalizeReference(firstReference, finalizeReferentMethod)) {
         return false;
      }

      Reference<?> furtherReference;
      do {
         furtherReference = this.queue.poll();
         if (furtherReference == null) {
            return true;
         }
      } while (this.finalizeReference(furtherReference, finalizeReferentMethod));

      return false;
   }

   private boolean finalizeReference(Reference<?> reference, Method finalizeReferentMethod) {
      reference.clear();
      if (reference == this.frqReference) {
         return false;
      }

      try {
         finalizeReferentMethod.invoke(reference);
      } catch (Throwable t) {
         logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
      }

      return true;
   }

   private @Nullable Method getFinalizeReferentMethod() {
      Class<?> finalizableReferenceClass = this.finalizableReferenceClassReference.get();
      if (finalizableReferenceClass == null) {
         return null;
      }

      try {
         return finalizableReferenceClass.getMethod("finalizeReferent");
      } catch (NoSuchMethodException e) {
         throw new AssertionError(e);
      }
   }

   private static @Nullable Field getInheritableThreadLocalsField() {
      try {
         Field inheritableThreadLocals = Thread.class.getDeclaredField("inheritableThreadLocals");
         inheritableThreadLocals.setAccessible(true);
         return inheritableThreadLocals;
      } catch (Throwable t) {
         logger.log(Level.INFO, "Couldn't access Thread.inheritableThreadLocals. Reference finalizer threads will inherit thread local values.");
         return null;
      }
   }

   private static @Nullable Constructor<Thread> getBigThreadConstructor() {
      try {
         return Thread.class.getConstructor(ThreadGroup.class, Runnable.class, String.class, long.class, boolean.class);
      } catch (Throwable t) {
         return null;
      }
   }
}
