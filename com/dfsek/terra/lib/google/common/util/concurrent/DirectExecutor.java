package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.concurrent.Executor;

@GwtCompatible
enum DirectExecutor implements Executor {
   INSTANCE;

   @Override
   public void execute(Runnable command) {
      command.run();
   }

   @Override
   public String toString() {
      return "MoreExecutors.directExecutor()";
   }
}
