package org.incendo.cloud.exception;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
public class CommandExecutionException extends IllegalArgumentException {
   private final CommandContext<?> commandContext;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public CommandExecutionException(final @NonNull Throwable cause) {
      this(cause, null);
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public CommandExecutionException(final @NonNull Throwable cause, final @Nullable CommandContext<?> commandContext) {
      super(cause);
      this.commandContext = commandContext;
   }

   @API(status = Status.STABLE)
   public @Nullable CommandContext<?> context() {
      return this.commandContext;
   }
}
