package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;

@FunctionalInterface
public interface IORunnable {
   static IORunnable noop() {
      return Constants.IO_RUNNABLE;
   }

   default Runnable asRunnable() {
      return () -> Uncheck.run(this);
   }

   void run() throws IOException;
}
