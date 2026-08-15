package org.incendo.cloud.exception;

import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

@API(status = Status.STABLE)
public class CommandParseException extends IllegalArgumentException {
   private final Object commandSender;
   private final List<CommandComponent<?>> currentChain;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   protected CommandParseException(final @NonNull Object commandSender, final @NonNull List<CommandComponent<?>> currentChain) {
      this.commandSender = commandSender;
      this.currentChain = currentChain;
   }

   public @NonNull Object commandSender() {
      return this.commandSender;
   }

   public @NonNull List<@NonNull CommandComponent<?>> currentChain() {
      return Collections.unmodifiableList(this.currentChain);
   }
}
