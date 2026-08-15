package org.incendo.cloud.bean;

import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandFactory;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.meta.CommandMeta;

@API(status = Status.STABLE)
public abstract class CommandBean<C> implements CommandExecutionHandler<C>, CommandFactory<C> {
   protected CommandBean() {
   }

   @Override
   public @NonNull List<@NonNull Command<? extends C>> createCommands(final @NonNull CommandManager<C> commandManager) {
      Command.Builder<C> builder = commandManager.commandBuilder(this.properties().name(), this.properties().aliases(), this.meta()).handler(this);
      return Collections.singletonList(this.configure(builder).build());
   }

   protected @NonNull CommandMeta meta() {
      return CommandMeta.builder().build();
   }

   protected abstract @NonNull CommandProperties properties();

   protected abstract Command.@NonNull Builder<? extends C> configure(Command.@NonNull Builder<C> builder);

   @Override
   public void execute(final @NonNull CommandContext<C> commandContext) {
   }
}
