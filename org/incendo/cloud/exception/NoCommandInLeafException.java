package org.incendo.cloud.exception;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

@API(status = Status.STABLE)
public final class NoCommandInLeafException extends IllegalStateException {
   private final CommandComponent<?> commandComponent;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public NoCommandInLeafException(final @NonNull CommandComponent<?> commandComponent) {
      super(String.format("Leaf node '%s' does not have associated owning command", commandComponent.name()));
      this.commandComponent = commandComponent;
   }

   public @NonNull CommandComponent<?> commandComponent() {
      return this.commandComponent;
   }
}
