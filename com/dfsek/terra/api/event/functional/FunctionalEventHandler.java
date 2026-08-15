package com.dfsek.terra.api.event.functional;

import com.dfsek.terra.api.addon.BaseAddon;
import com.dfsek.terra.api.event.EventHandler;
import com.dfsek.terra.api.event.events.Event;
import com.dfsek.terra.api.util.reflection.TypeKey;

public interface FunctionalEventHandler extends EventHandler {
   <T extends Event> EventContext<T> register(BaseAddon var1, Class<T> var2);

   <T extends Event> EventContext<T> register(BaseAddon var1, TypeKey<T> var2);
}
