package com.dfsek.terra.lib.yaml.snakeyaml.constructor;

import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;

public class DuplicateKeyException extends ConstructorException {
   protected DuplicateKeyException(Mark contextMark, Object key, Mark problemMark) {
      super("while constructing a mapping", contextMark, "found duplicate key " + key, problemMark);
   }
}
