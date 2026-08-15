package org.incendo.cloud.context;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class StandardCommandContextFactory<C> implements CommandContextFactory<C> {
   private final CommandManager<C> commandManager;

   public StandardCommandContextFactory(final @NonNull CommandManager<C> commandManager) {
      this.commandManager = commandManager;
   }

   @Override
   public @NonNull CommandContext<C> create(final boolean suggestions, final @NonNull C sender) {
      return new CommandContext<>(suggestions, sender, this.commandManager);
   }
}
