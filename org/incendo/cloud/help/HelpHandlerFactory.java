package org.incendo.cloud.help;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;

@API(status = Status.STABLE)
public interface HelpHandlerFactory<C> {
   static <C> @NonNull HelpHandlerFactory<C> standard(final @NonNull CommandManager<C> commandManager) {
      return new StandardHelpHandlerFactory<>(commandManager);
   }

   @NonNull HelpHandler<C> createHelpHandler(@NonNull CommandPredicate<C> filter);
}
