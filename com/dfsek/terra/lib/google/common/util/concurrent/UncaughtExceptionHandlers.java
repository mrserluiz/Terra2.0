package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.logging.Level;

@J2ktIncompatible
@GwtIncompatible
public final class UncaughtExceptionHandlers {
   private UncaughtExceptionHandlers() {
   }

   public static UncaughtExceptionHandler systemExit() {
      return new UncaughtExceptionHandlers.Exiter(Runtime.getRuntime()::exit);
   }

   @VisibleForTesting
   static final class Exiter implements UncaughtExceptionHandler {
      private static final LazyLogger logger = new LazyLogger(UncaughtExceptionHandlers.Exiter.class);
      private final UncaughtExceptionHandlers.RuntimeWrapper runtime;

      Exiter(UncaughtExceptionHandlers.RuntimeWrapper runtime) {
         this.runtime = runtime;
      }

      @Override
      public void uncaughtException(Thread t, Throwable e) {
         try {
            logger.get().log(Level.SEVERE, String.format(Locale.ROOT, "Caught an exception in %s.  Shutting down.", t), e);
         } catch (Throwable errorInLogging) {
            System.err.println(e.getMessage());
            System.err.println(errorInLogging.getMessage());
         } finally {
            this.runtime.exit(1);
         }
      }
   }

   @VisibleForTesting
   interface RuntimeWrapper {
      void exit(int status);
   }
}
