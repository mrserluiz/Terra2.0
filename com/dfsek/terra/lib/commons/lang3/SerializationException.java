package com.dfsek.terra.lib.commons.lang3;

public class SerializationException extends RuntimeException {
   private static final long serialVersionUID = 4029025366392702726L;

   public SerializationException() {
   }

   public SerializationException(String msg) {
      super(msg);
   }

   public SerializationException(String msg, Throwable cause) {
      super(msg, cause);
   }

   public SerializationException(Throwable cause) {
      super(cause);
   }
}
