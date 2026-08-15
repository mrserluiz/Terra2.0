package com.dfsek.terra.lib.yaml.snakeyaml.events;

import com.dfsek.terra.lib.yaml.snakeyaml.DumperOptions;
import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;

public final class SequenceStartEvent extends CollectionStartEvent {
   public SequenceStartEvent(String anchor, String tag, boolean implicit, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle) {
      super(anchor, tag, implicit, startMark, endMark, flowStyle);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.SequenceStart;
   }
}
