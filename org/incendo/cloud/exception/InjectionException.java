package org.incendo.cloud.exception;

import org.checkerframework.checker.nullness.qual.NonNull;

public class InjectionException extends RuntimeException {
   public InjectionException(final @NonNull String message, final @NonNull Throwable cause) {
      super(message, cause);
   }
}
