package com.dfsek.terra.api.event.events;

public interface Cancellable extends Event {
   boolean isCancelled();

   void setCancelled(boolean var1);
}
