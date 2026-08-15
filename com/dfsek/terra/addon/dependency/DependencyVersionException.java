package com.dfsek.terra.addon.dependency;

public class DependencyVersionException extends DependencyException {
   private static final long serialVersionUID = 3564288935278878135L;

   public DependencyVersionException(String message) {
      super(message);
   }

   public DependencyVersionException(String message, Throwable cause) {
      super(message, cause);
   }
}
