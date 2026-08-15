package org.incendo.cloud;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface CloudCapability {
   @Override
   @NonNull String toString();

   @API(status = Status.STABLE)
   final class CloudCapabilityMissingException extends RuntimeException {
      public CloudCapabilityMissingException(final @NonNull CloudCapability capability) {
         super(String.format("Missing capability '%s'", capability));
      }
   }

   @API(status = Status.STABLE)
   enum StandardCapabilities implements CloudCapability {
      ROOT_COMMAND_DELETION;

      @Override
      public @NonNull String toString() {
         return this.name();
      }
   }
}
