package com.dfsek.terra.addon.dependency;

public class DependencyException extends RuntimeException {
   private static final long serialVersionUID = 4864727433635612759L;

   public DependencyException(String message) {
      super(message);
   }

   public DependencyException(String message, Throwable cause) {
      super(message, cause);
   }
}
