package com.dfsek.terra.event;

import com.dfsek.terra.api.addon.BaseAddon;
import com.dfsek.terra.api.event.events.Event;
import com.dfsek.terra.api.event.events.FailThroughEvent;
import com.dfsek.terra.api.event.functional.EventContext;
import com.dfsek.terra.api.util.reflection.ReflectionUtil;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class EventContextImpl<T extends Event> implements EventContext<T>, Comparable<EventContextImpl<?>> {
   private final List<Consumer<T>> actions = new ArrayList<>();
   private final BaseAddon addon;
   private final Type eventType;
   private final FunctionalEventHandlerImpl parent;
   private int priority;
   private boolean failThrough = false;
   private boolean global = false;

   public EventContextImpl(BaseAddon addon, Type eventType, FunctionalEventHandlerImpl parent) {
      this.addon = addon;
      this.eventType = eventType;
      this.parent = parent;
   }

   public void handle(T event) {
      this.actions.forEach(action -> action.accept(event));
   }

   @Override
   public EventContext<T> then(Consumer<T> action) {
      this.actions.add(action);
      return this;
   }

   @Override
   public EventContext<T> priority(int priority) {
      this.priority = priority;
      this.parent.recomputePriorities(this.eventType);
      return this;
   }

   @Override
   public EventContext<T> failThrough() {
      if (!FailThroughEvent.class.isAssignableFrom(ReflectionUtil.getRawType(this.eventType))) {
         throw new IllegalStateException(
            "Cannot fail-through on event which does not implement FailThroughEvent: " + ReflectionUtil.typeToString(this.eventType)
         );
      }

      this.failThrough = true;
      return this;
   }

   @Override
   public EventContext<T> global() {
      this.global = true;
      return this;
   }

   public int compareTo(@NotNull EventContextImpl<?> o) {
      return this.priority - o.priority;
   }

   public boolean isGlobal() {
      return this.global;
   }

   public int getPriority() {
      return this.priority;
   }

   public BaseAddon getAddon() {
      return this.addon;
   }

   public boolean isFailThrough() {
      return this.failThrough;
   }
}
