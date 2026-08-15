package org.incendo.cloud.exception;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

@API(status = Status.STABLE)
public class InvalidSyntaxException extends CommandParseException {
   private final String correctSyntax;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public InvalidSyntaxException(
      final @NonNull String correctSyntax, final @NonNull Object commandSender, final @NonNull List<@NonNull CommandComponent<?>> currentChain
   ) {
      super(commandSender, currentChain);
      this.correctSyntax = correctSyntax;
   }

   public @NonNull String correctSyntax() {
      return this.correctSyntax;
   }

   @Override
   public final String getMessage() {
      return String.format("Invalid command syntax. Correct syntax is: %s", this.correctSyntax);
   }
}
