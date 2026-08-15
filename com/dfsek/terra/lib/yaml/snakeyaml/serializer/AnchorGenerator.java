package com.dfsek.terra.lib.yaml.snakeyaml.serializer;

import com.dfsek.terra.lib.yaml.snakeyaml.nodes.Node;

public interface AnchorGenerator {
   String nextAnchor(Node var1);
}
