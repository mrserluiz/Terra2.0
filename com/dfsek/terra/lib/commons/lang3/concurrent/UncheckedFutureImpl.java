package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.exception.UncheckedInterruptedException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class UncheckedFutureImpl<V> extends AbstractFutureProxy<V> implements UncheckedFuture<V> {
   UncheckedFutureImpl(Future<V> future) {
      super(future);
   }

   @Override
   public V get() {
      try {
         return super.get();
      } catch (InterruptedException e) {
         throw new UncheckedInterruptedException(e);
      } catch (ExecutionException e) {
         throw new UncheckedExecutionException(e);
      }
   }

   @Override
   public V get(long timeout, TimeUnit unit) {
      try {
         return super.get(timeout, unit);
      } catch (InterruptedException e) {
         throw new UncheckedInterruptedException(e);
      } catch (ExecutionException e) {
         throw new UncheckedExecutionException(e);
      } catch (TimeoutException e) {
         throw new UncheckedTimeoutException(e);
      }
   }
}
