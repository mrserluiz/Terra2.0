package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class CallableBackgroundInitializer<T> extends BackgroundInitializer<T> {
   private final Callable<T> callable;

   public CallableBackgroundInitializer(Callable<T> call) {
      this.checkCallable(call);
      this.callable = call;
   }

   public CallableBackgroundInitializer(Callable<T> call, ExecutorService exec) {
      super(exec);
      this.checkCallable(call);
      this.callable = call;
   }

   private void checkCallable(Callable<T> callable) {
      Objects.requireNonNull(callable, "callable");
   }

   @Override
   protected Exception getTypedException(Exception e) {
      return new Exception(e);
   }

   @Override
   protected T initialize() throws Exception {
      return this.callable.call();
   }
}
