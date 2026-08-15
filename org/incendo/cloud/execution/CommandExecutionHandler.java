package org.incendo.cloud.execution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;

@FunctionalInterface
@API(status = Status.STABLE)
public interface CommandExecutionHandler<C> {
   @API(status = Status.STABLE)
   static <C> @NonNull CommandExecutionHandler<C> noOpCommandExecutionHandler() {
      return (CommandExecutionHandler<C>)NullCommandExecutionHandler.INSTANCE;
   }

   @API(status = Status.STABLE)
   static <C> @NonNull CommandExecutionHandler<C> delegatingExecutionHandler(final List<CommandExecutionHandler<C>> handlers) {
      return new MulticastDelegateFutureCommandExecutionHandler<>(handlers);
   }

   void execute(@NonNull CommandContext<C> commandContext);

   @API(status = Status.STABLE)
   default CompletableFuture<@Nullable Void> executeFuture(@NonNull CommandContext<C> commandContext) {
      CompletableFuture<Void> future = new CompletableFuture<>();

      try {
         this.execute(commandContext);
         future.complete(null);
      } catch (Throwable throwable) {
         future.completeExceptionally(throwable);
      }

      return future;
   }

   @FunctionalInterface
   @API(status = Status.STABLE)
   interface FutureCommandExecutionHandler<C> extends CommandExecutionHandler<C> {
      @Override
      default void execute(@NonNull CommandContext<C> commandContext) {
         throw new UnsupportedOperationException("execute should not be called on FutureCommandExecutionHandlers, call executeFuture instead.");
      }

      @Override
      CompletableFuture<@Nullable Void> executeFuture(@NonNull CommandContext<C> commandContext);
   }
}
