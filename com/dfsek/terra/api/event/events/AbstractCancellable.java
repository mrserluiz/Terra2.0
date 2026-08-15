package com.dfsek.terra.api.event.events;

import com.dfsek.terra.api.util.mutable.MutableBoolean;

public abstract class AbstractCancellable implements Cancellable {
   private final MutableBoolean cancelled = new MutableBoolean(false);

   @Override
   public boolean isCancelled() {
      return this.cancelled.get();
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled.set(cancelled);
   }
}
