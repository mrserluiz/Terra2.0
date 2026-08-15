package org.incendo.cloud;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandContextFactory;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.execution.CommandExecutor;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.services.State;
import org.incendo.cloud.util.CompletableFutures;

final class StandardCommandExecutor<C> implements CommandExecutor<C> {
   private final CommandManager<C> commandManager;
   private final ExecutionCoordinator<C> executionCoordinator;
   private final CommandContextFactory<C> commandContextFactory;

   StandardCommandExecutor(
      final @NonNull CommandManager<C> commandManager,
      final @NonNull ExecutionCoordinator<C> executionCoordinator,
      final @NonNull CommandContextFactory<C> commandContextFactory
   ) {
      this.commandManager = commandManager;
      this.executionCoordinator = executionCoordinator;
      this.commandContextFactory = commandContextFactory;
   }

   @Override
   public @NonNull CompletableFuture<CommandResult<C>> executeCommand(
      final @NonNull C commandSender, final @NonNull String input, final @NonNull Consumer<CommandContext<C>> contextConsumer
   ) {
      CommandContext<C> context = this.commandContextFactory.create(false, commandSender);
      contextConsumer.accept(context);
      CommandInput commandInput = CommandInput.of(input);
      return this.executeCommand(context, commandInput).whenComplete((result, throwable) -> {
         if (throwable != null) {
            try {
               this.commandManager.exceptionController().handleException(context, ExceptionController.unwrapCompletionException(throwable));
            } catch (RuntimeException runtimeException) {
               throw runtimeException;
            } catch (Throwable e) {
               throw new CompletionException(e);
            }
         }
      });
   }

   private @NonNull CompletableFuture<CommandResult<C>> executeCommand(final @NonNull CommandContext<C> context, final @NonNull CommandInput commandInput) {
      context.store("__raw_input__", commandInput.copy());

      try {
         if (this.commandManager.preprocessContext(context, commandInput) == State.ACCEPTED) {
            return this.executionCoordinator().coordinateExecution(this.commandManager.commandTree(), context, commandInput);
         }
      } catch (Exception e) {
         return CompletableFutures.failedFuture(e);
      }

      return CompletableFuture.completedFuture(null);
   }

   @Override
   public @NonNull ExecutionCoordinator<C> executionCoordinator() {
      return this.executionCoordinator;
   }
}
