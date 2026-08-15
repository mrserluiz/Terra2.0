package org.incendo.cloud.execution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.CommandParseException;
import org.incendo.cloud.services.State;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.type.tuple.Pair;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
final class ExecutionCoordinatorImpl<C> implements ExecutionCoordinator<C> {
   static final Executor NON_SCHEDULING_EXECUTOR = new ExecutionCoordinatorImpl.NonSchedulingExecutor();
   private final @NonNull Executor parsingExecutor;
   private final @NonNull Executor suggestionsExecutor;
   private final @NonNull Executor defaultExecutionExecutor;
   private final @Nullable Semaphore executionLock;

   ExecutionCoordinatorImpl(
      final @Nullable Executor parsingExecutor,
      final @Nullable Executor suggestionsExecutor,
      final @Nullable Executor defaultExecutionExecutor,
      final boolean syncExecution
   ) {
      this.parsingExecutor = orRunNow(parsingExecutor);
      this.suggestionsExecutor = orRunNow(suggestionsExecutor);
      this.defaultExecutionExecutor = orRunNow(defaultExecutionExecutor);
      this.executionLock = syncExecution ? new Semaphore(1) : null;
   }

   private static @NonNull Executor orRunNow(final @Nullable Executor e) {
      return e == null ? ExecutionCoordinator.nonSchedulingExecutor() : e;
   }

   @Override
   public @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(
      final @NonNull CommandTree<C> commandTree, final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput
   ) {
      return commandTree.parse(commandContext, commandInput, this.parsingExecutor).thenApplyAsync(command -> {
         boolean passedPostprocessing = commandTree.commandManager().postprocessContext(commandContext, (Command<C>)command) == State.ACCEPTED;
         return Pair.of(command, passedPostprocessing);
      }, this.parsingExecutor).thenComposeAsync(preprocessResult -> {
         if (!preprocessResult.second()) {
            return CompletableFuture.completedFuture(CommandResult.of(commandContext));
         }

         if (this.executionLock != null) {
            try {
               this.executionLock.acquire();
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         }

         CompletableFuture<CommandResult<C>> commandResultFuture = null;

         try {
            commandResultFuture = preprocessResult.first().commandExecutionHandler().executeFuture(commandContext).exceptionally(exception -> {
               Throwable workingException;
               if (exception instanceof CompletionException) {
                  workingException = exception.getCause();
               } else {
                  workingException = exception;
               }

               if (workingException instanceof CommandParseException) {
                  throw (CommandParseException)workingException;
               } else if (workingException instanceof CommandExecutionException) {
                  throw (CommandExecutionException)workingException;
               } else {
                  throw new CommandExecutionException(workingException, commandContext);
               }
            }).thenApply(v -> CommandResult.of(commandContext));
         } finally {
            if (this.executionLock != null) {
               if (commandResultFuture != null) {
                  commandResultFuture.whenComplete(($, $$) -> this.executionLock.release());
               } else {
                  this.executionLock.release();
               }
            }
         }

         return commandResultFuture;
      }, this.defaultExecutionExecutor);
   }

   @Override
   public <S extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> coordinateSuggestions(
      final @NonNull CommandTree<C> commandTree,
      final @NonNull CommandContext<C> context,
      final @NonNull CommandInput commandInput,
      final @NonNull SuggestionMapper<S> mapper
   ) {
      return commandTree.getSuggestions(context, commandInput, mapper, this.suggestionsExecutor);
   }

   private static final class NonSchedulingExecutor implements Executor {
      private NonSchedulingExecutor() {
      }

      @Override
      public void execute(final Runnable command) {
         command.run();
      }
   }
}
