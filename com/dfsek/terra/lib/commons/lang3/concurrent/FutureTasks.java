package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class FutureTasks {
   public static <V> FutureTask<V> run(Callable<V> callable) {
      FutureTask<V> futureTask = new FutureTask<>(callable);
      futureTask.run();
      return futureTask;
   }

   private FutureTasks() {
   }
}
