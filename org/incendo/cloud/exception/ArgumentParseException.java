package org.incendo.cloud.exception;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

@API(status = Status.STABLE)
public class ArgumentParseException extends CommandParseException {
   private final Throwable cause;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public ArgumentParseException(
      final @NonNull Throwable throwable, final @NonNull Object commandSender, final @NonNull List<@NonNull CommandComponent<?>> currentChain
   ) {
      super(commandSender, currentChain);
      this.cause = throwable;
   }

   @Override
   public synchronized @NonNull Throwable getCause() {
      return this.cause;
   }
}
