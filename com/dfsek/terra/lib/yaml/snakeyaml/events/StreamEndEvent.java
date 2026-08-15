package com.dfsek.terra.lib.yaml.snakeyaml.events;

import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;

public final class StreamEndEvent extends Event {
   public StreamEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.StreamEnd;
   }
}
