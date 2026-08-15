package com.dfsek.terra.lib.yaml.snakeyaml.composer;

import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;
import com.dfsek.terra.lib.yaml.snakeyaml.error.MarkedYAMLException;

public class ComposerException extends MarkedYAMLException {
   private static final long serialVersionUID = 2146314636913113935L;

   protected ComposerException(String context, Mark contextMark, String problem, Mark problemMark) {
      super(context, contextMark, problem, problemMark);
   }
}
