package org.incendo.cloud.help;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
final class StandardHelpHandlerFactory<C> implements HelpHandlerFactory<C> {
   private final CommandManager<C> commandManager;

   StandardHelpHandlerFactory(final @NonNull CommandManager<C> commandManager) {
      this.commandManager = commandManager;
   }

   @Override
   public @NonNull HelpHandler<C> createHelpHandler(final @NonNull CommandPredicate<C> filter) {
      return new StandardHelpHandler<>(this.commandManager, filter);
   }
}
