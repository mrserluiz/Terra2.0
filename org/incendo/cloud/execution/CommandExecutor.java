package org.incendo.cloud.execution;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
public interface CommandExecutor<C> {
   default @NonNull CompletableFuture<CommandResult<C>> executeCommand(final @NonNull C commandSender, final @NonNull String input) {
      return this.executeCommand(commandSender, input, context -> {});
   }

   @NonNull CompletableFuture<CommandResult<C>> executeCommand(
      @NonNull C commandSender, @NonNull String input, @NonNull Consumer<CommandContext<C>> contextConsumer
   );

   @NonNull ExecutionCoordinator<C> executionCoordinator();
}
