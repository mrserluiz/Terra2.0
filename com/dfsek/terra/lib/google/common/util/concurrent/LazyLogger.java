package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class LazyLogger {
   private final Object lock = new Object();
   private final String loggerName;
   private volatile @Nullable Logger logger;

   LazyLogger(Class<?> ownerOfLogger) {
      this.loggerName = ownerOfLogger.getName();
   }

   Logger get() {
      Logger local = this.logger;
      if (local != null) {
         return local;
      }

      synchronized (this.lock) {
         local = this.logger;
         return local != null ? local : (this.logger = Logger.getLogger(this.loggerName));
      }
   }
}
