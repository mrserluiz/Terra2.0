package org.incendo.cloud.execution;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.INTERNAL)
final class NullCommandExecutionHandler<C> implements CommandExecutionHandler<C> {
   static final CommandExecutionHandler<?> INSTANCE = new NullCommandExecutionHandler();

   @Override
   public void execute(final @NonNull CommandContext<C> commandContext) {
   }
}
