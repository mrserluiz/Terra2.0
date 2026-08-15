package org.incendo.cloud.internal;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;

@FunctionalInterface
@API(status = Status.STABLE)
public interface CommandRegistrationHandler<C> {
   static <C> @NonNull CommandRegistrationHandler<C> nullCommandRegistrationHandler() {
      return new CommandRegistrationHandler.NullCommandRegistrationHandler<>();
   }

   boolean registerCommand(@NonNull Command<C> command);

   @API(status = Status.STABLE)
   default void unregisterRootCommand(final @NonNull CommandComponent<C> rootCommand) {
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   final class NullCommandRegistrationHandler<C> implements CommandRegistrationHandler<C> {
      private NullCommandRegistrationHandler() {
      }

      @Override
      public boolean registerCommand(final @NonNull Command<C> command) {
         return true;
      }

      @Override
      public void unregisterRootCommand(final @NonNull CommandComponent<C> rootCommand) {
      }
   }
}
