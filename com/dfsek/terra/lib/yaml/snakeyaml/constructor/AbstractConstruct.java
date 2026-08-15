package com.dfsek.terra.lib.yaml.snakeyaml.constructor;

import com.dfsek.terra.lib.yaml.snakeyaml.error.YAMLException;
import com.dfsek.terra.lib.yaml.snakeyaml.nodes.Node;

public abstract class AbstractConstruct implements Construct {
   @Override
   public void construct2ndStep(Node node, Object data) {
      if (node.isTwoStepsConstruction()) {
         throw new IllegalStateException("Not Implemented in " + this.getClass().getName());
      } else {
         throw new YAMLException("Unexpected recursive structure for Node: " + node);
      }
   }
}
