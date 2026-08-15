package org.incendo.cloud.execution;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
@Immutable
public interface CommandResult<C> {
   static <C> @NonNull CommandResult<C> of(final @NonNull CommandContext<C> context) {
      return CommandResultImpl.of(context);
   }

   @NonNull CommandContext<C> commandContext();
}
