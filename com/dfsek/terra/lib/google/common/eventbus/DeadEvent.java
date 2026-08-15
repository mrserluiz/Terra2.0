package com.dfsek.terra.lib.google.common.eventbus;

import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Preconditions;

public class DeadEvent {
   private final Object source;
   private final Object event;

   public DeadEvent(Object source, Object event) {
      this.source = Preconditions.checkNotNull(source);
      this.event = Preconditions.checkNotNull(event);
   }

   public Object getSource() {
      return this.source;
   }

   public Object getEvent() {
      return this.event;
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).add("source", this.source).add("event", this.event).toString();
   }
}
