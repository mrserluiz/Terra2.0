package com.dfsek.terra.lib.yaml.snakeyaml.events;

import com.dfsek.terra.lib.yaml.snakeyaml.DumperOptions;
import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;
import java.util.Map;

public final class DocumentStartEvent extends Event {
   private final boolean explicit;
   private final DumperOptions.Version version;
   private final Map<String, String> tags;

   public DocumentStartEvent(Mark startMark, Mark endMark, boolean explicit, DumperOptions.Version version, Map<String, String> tags) {
      super(startMark, endMark);
      this.explicit = explicit;
      this.version = version;
      this.tags = tags;
   }

   public boolean getExplicit() {
      return this.explicit;
   }

   public DumperOptions.Version getVersion() {
      return this.version;
   }

   public Map<String, String> getTags() {
      return this.tags;
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.DocumentStart;
   }
}
