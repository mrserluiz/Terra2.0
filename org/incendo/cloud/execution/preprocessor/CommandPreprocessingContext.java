package org.incendo.cloud.execution.preprocessor;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@API(status = Status.STABLE)
@Immutable
public interface CommandPreprocessingContext<C> {
   static <C> @NonNull CommandPreprocessingContext<C> of(final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput) {
      return CommandPreprocessingContextImpl.of(commandContext, commandInput);
   }

   @NonNull CommandContext<@NonNull C> commandContext();

   @NonNull CommandInput commandInput();
}
