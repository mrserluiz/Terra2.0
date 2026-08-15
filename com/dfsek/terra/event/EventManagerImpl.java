package com.dfsek.terra.event;

import com.dfsek.terra.api.event.EventHandler;
import com.dfsek.terra.api.event.EventManager;
import com.dfsek.terra.api.event.events.Event;
import com.dfsek.terra.api.event.functional.FunctionalEventHandler;
import java.util.HashMap;
import java.util.Map;

public class EventManagerImpl implements EventManager {
   private final Map<Class<?>, EventHandler> handlers = new HashMap<>();

   public EventManagerImpl() {
      this.registerHandler(FunctionalEventHandler.class, new FunctionalEventHandlerImpl());
   }

   @Override
   public <T extends Event> T callEvent(T event) {
      this.handlers.values().forEach(handler -> handler.handle(event));
      return event;
   }

   @Override
   public <T extends EventHandler> void registerHandler(Class<T> clazz, T handler) {
      this.handlers.put(clazz, handler);
   }

   @Override
   public <T extends EventHandler> T getHandler(Class<T> clazz) {
      return (T)this.handlers.computeIfAbsent(clazz, c -> {
         throw new IllegalArgumentException("No event handler registered for class " + clazz.getCanonicalName());
      });
   }
}
