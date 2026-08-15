package com.dfsek.terra.lib.yaml.snakeyaml.constructor;

import com.dfsek.terra.lib.yaml.snakeyaml.nodes.Node;

public interface Construct {
   Object construct(Node var1);

   void construct2ndStep(Node var1, Object var2);
}
