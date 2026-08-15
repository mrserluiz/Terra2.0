package com.dfsek.terra.lib.yaml.snakeyaml.parser;

import com.dfsek.terra.lib.yaml.snakeyaml.events.Event;

interface Production {
   Event produce();
}
