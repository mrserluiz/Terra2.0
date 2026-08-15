package org.incendo.cloud.exception;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

@API(status = Status.STABLE)
public final class NoSuchCommandException extends CommandParseException {
   private final String suppliedCommand;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public NoSuchCommandException(final @NonNull Object commandSender, final @NonNull List<CommandComponent<?>> currentChain, final @NonNull String command) {
      super(commandSender, currentChain);
      this.suppliedCommand = command;
   }

   @Override
   public String getMessage() {
      StringBuilder builder = new StringBuilder();

      for (CommandComponent<?> commandComponent : this.currentChain()) {
         if (commandComponent != null) {
            builder.append(" ").append(commandComponent.name());
         }
      }

      return String.format("Unrecognized command input '%s' following chain%s", this.suppliedCommand, builder.toString());
   }

   public @NonNull String suppliedCommand() {
      return this.suppliedCommand;
   }

   @Override
   public synchronized Throwable fillInStackTrace() {
      return this;
   }

   @Override
   public synchronized Throwable initCause(final Throwable cause) {
      return this;
   }
}
