package com.dfsek.terra.lib.yaml.snakeyaml.emitter;

import com.dfsek.terra.lib.yaml.snakeyaml.events.Event;
import java.io.IOException;

public interface Emitable {
   void emit(Event var1) throws IOException;
}
