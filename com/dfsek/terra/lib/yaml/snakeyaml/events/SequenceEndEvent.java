package com.dfsek.terra.lib.yaml.snakeyaml.events;

import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;

public final class SequenceEndEvent extends CollectionEndEvent {
   public SequenceEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.SequenceEnd;
   }
}
