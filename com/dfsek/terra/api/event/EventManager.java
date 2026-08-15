package com.dfsek.terra.api.event;

import com.dfsek.terra.api.event.events.Event;

public interface EventManager {
   <T extends Event> T callEvent(T var1);

   <T extends EventHandler> void registerHandler(Class<T> var1, T var2);

   <T extends EventHandler> T getHandler(Class<T> var1);
}
