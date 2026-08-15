package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public final class SettableFuture<V> extends AbstractFuture.TrustedFuture<V> {
   public static <V> SettableFuture<V> create() {
      return new SettableFuture<>();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean set(@ParametricNullness V value) {
      return super.set(value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean setException(Throwable throwable) {
      return super.setException(throwable);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean setFuture(ListenableFuture<? extends V> future) {
      return super.setFuture(future);
   }

   private SettableFuture() {
   }
}
