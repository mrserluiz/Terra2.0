package com.dfsek.terra.lib.yaml.snakeyaml.serializer;

import com.dfsek.terra.lib.yaml.snakeyaml.error.YAMLException;

public class SerializerException extends YAMLException {
   private static final long serialVersionUID = 2632638197498912433L;

   public SerializerException(String message) {
      super(message);
   }
}
