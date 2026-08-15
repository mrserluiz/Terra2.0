package com.dfsek.terra.addon.dependency;

public class CircularDependencyException extends DependencyException {
   private static final long serialVersionUID = -6098780459461482651L;

   public CircularDependencyException(String message) {
      super(message);
   }

   public CircularDependencyException(String message, Throwable cause) {
      super(message, cause);
   }
}
