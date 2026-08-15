package com.dfsek.terra.lib.yaml.snakeyaml.parser;

import com.dfsek.terra.lib.yaml.snakeyaml.events.Event;

public interface Parser {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event getEvent();
}
