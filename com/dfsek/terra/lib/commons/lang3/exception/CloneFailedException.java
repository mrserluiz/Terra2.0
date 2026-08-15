package com.dfsek.terra.lib.commons.lang3.exception;

public class CloneFailedException extends RuntimeException {
   private static final long serialVersionUID = 20091223L;

   public CloneFailedException(String message) {
      super(message);
   }

   public CloneFailedException(String message, Throwable cause) {
      super(message, cause);
   }

   public CloneFailedException(Throwable cause) {
      super(cause);
   }
}
