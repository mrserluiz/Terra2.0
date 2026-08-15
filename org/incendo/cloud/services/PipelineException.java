package org.incendo.cloud.services;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class PipelineException extends RuntimeException {
   public PipelineException(final @NonNull Exception cause) {
      super(cause);
   }

   public PipelineException(final @NonNull String message, final @NonNull Exception cause) {
      super(message, cause);
   }
}
