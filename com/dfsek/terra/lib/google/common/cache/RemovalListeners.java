package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.concurrent.Executor;

@GwtIncompatible
public final class RemovalListeners {
   private RemovalListeners() {
   }

   public static <K, V> RemovalListener<K, V> asynchronous(RemovalListener<K, V> listener, Executor executor) {
      Preconditions.checkNotNull(listener);
      Preconditions.checkNotNull(executor);
      return notification -> executor.execute(() -> listener.onRemoval(notification));
   }
}
