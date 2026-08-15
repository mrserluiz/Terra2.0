package com.dfsek.terra.lib.yaml.snakeyaml.events;

import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }
}
