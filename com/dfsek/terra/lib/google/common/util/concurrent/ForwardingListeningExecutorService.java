package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.util.concurrent.Callable;

@J2ktIncompatible
@GwtIncompatible
public abstract class ForwardingListeningExecutorService extends ForwardingExecutorService implements ListeningExecutorService {
   protected ForwardingListeningExecutorService() {
   }

   protected abstract ListeningExecutorService delegate();

   @Override
   public <T> ListenableFuture<T> submit(Callable<T> task) {
      return this.delegate().submit(task);
   }

   @Override
   public ListenableFuture<?> submit(Runnable task) {
      return this.delegate().submit(task);
   }

   @Override
   public <T> ListenableFuture<T> submit(Runnable task, @ParametricNullness T result) {
      return this.delegate().submit(task, result);
   }
}
