package org.incendo.cloud.execution.postprocessor;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
@Immutable
public interface CommandPostprocessingContext<C> {
   static <C> @NonNull CommandPostprocessingContext<C> of(final @NonNull CommandContext<C> commandContext, final @NonNull Command<C> command) {
      return CommandPostprocessingContextImpl.of(commandContext, command);
   }

   @NonNull CommandContext<@NonNull C> commandContext();

   @NonNull Command<@NonNull C> command();
}
