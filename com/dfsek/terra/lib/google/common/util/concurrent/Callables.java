package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Supplier;
import java.util.concurrent.Callable;

@GwtCompatible(emulated = true)
public final class Callables {
   private Callables() {
   }

   public static <T> Callable<T> returning(@ParametricNullness T value) {
      return () -> value;
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <T> AsyncCallable<T> asAsyncCallable(Callable<T> callable, ListeningExecutorService listeningExecutorService) {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(listeningExecutorService);
      return () -> listeningExecutorService.submit(callable);
   }

   @J2ktIncompatible
   @GwtIncompatible
   static <T> Callable<T> threadRenaming(Callable<T> callable, Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(nameSupplier);
      Preconditions.checkNotNull(callable);
      return () -> {
         Thread currentThread = Thread.currentThread();
         String oldName = currentThread.getName();
         boolean restoreName = trySetName(nameSupplier.get(), currentThread);

         try {
            return callable.call();
         } finally {
            if (restoreName) {
               boolean var8 = trySetName(oldName, currentThread);
            }
         }
      };
   }

   @J2ktIncompatible
   @GwtIncompatible
   static Runnable threadRenaming(Runnable task, Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(nameSupplier);
      Preconditions.checkNotNull(task);
      return () -> {
         Thread currentThread = Thread.currentThread();
         String oldName = currentThread.getName();
         boolean restoreName = trySetName(nameSupplier.get(), currentThread);

         try {
            task.run();
         } finally {
            if (restoreName) {
               boolean var7 = trySetName(oldName, currentThread);
            }
         }
      };
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static boolean trySetName(String threadName, Thread currentThread) {
      try {
         currentThread.setName(threadName);
         return true;
      } catch (SecurityException e) {
         return false;
      }
   }
}
