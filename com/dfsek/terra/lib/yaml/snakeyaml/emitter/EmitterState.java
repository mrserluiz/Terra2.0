package com.dfsek.terra.lib.yaml.snakeyaml.emitter;

import java.io.IOException;

interface EmitterState {
   void expect() throws IOException;
}
