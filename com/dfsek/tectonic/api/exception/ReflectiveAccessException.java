package com.dfsek.tectonic.api.exception;

public class ReflectiveAccessException extends RuntimeException {
   private static final long serialVersionUID = -2960519562022569730L;

   public ReflectiveAccessException(String message, ReflectiveOperationException cause) {
      super(message, cause);
   }
}
