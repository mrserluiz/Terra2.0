package org.incendo.cloud.execution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Pure;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.Suggestions;

@API(status = Status.STABLE)
public interface ExecutionCoordinator<C> {
   @Pure
   static <C> ExecutionCoordinator.@NonNull Builder<C> builder() {
      return new ExecutionCoordinatorBuilderImpl<>();
   }

   @Pure
   static <C> @NonNull ExecutionCoordinator<C> simpleCoordinator() {
      return builder().build();
   }

   @Pure
   static <C> @NonNull ExecutionCoordinator<C> coordinatorFor(final @NonNull Executor executor) {
      return builder().executor(executor).build();
   }

   @Pure
   static <C> @NonNull ExecutionCoordinator<C> asyncCoordinator() {
      return builder().commonPoolExecutor().build();
   }

   @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(
      @NonNull CommandTree<C> commandTree, @NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput
   );

   <S extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> coordinateSuggestions(
      @NonNull CommandTree<C> commandTree, @NonNull CommandContext<C> context, @NonNull CommandInput commandInput, @NonNull SuggestionMapper<S> mapper
   );

   @Pure
   static @NonNull Executor nonSchedulingExecutor() {
      return ExecutionCoordinatorImpl.NON_SCHEDULING_EXECUTOR;
   }

   @API(status = Status.STABLE)
   interface Builder<C> {
      default ExecutionCoordinator.@This @NonNull Builder<C> executor(final @NonNull Executor executor) {
         return this.parsingExecutor(executor).suggestionsExecutor(executor).executionSchedulingExecutor(executor);
      }

      default ExecutionCoordinator.@This @NonNull Builder<C> commonPoolExecutor() {
         return this.executor(ForkJoinPool.commonPool());
      }

      ExecutionCoordinator.@This @NonNull Builder<C> parsingExecutor(@NonNull Executor executor);

      ExecutionCoordinator.@This @NonNull Builder<C> suggestionsExecutor(@NonNull Executor executor);

      ExecutionCoordinator.@This @NonNull Builder<C> executionSchedulingExecutor(@NonNull Executor executor);

      default ExecutionCoordinator.@This @NonNull Builder<C> synchronizeExecution() {
         return this.synchronizeExecution(true);
      }

      ExecutionCoordinator.@This @NonNull Builder<C> synchronizeExecution(boolean synchronizeExecution);

      @NonNull ExecutionCoordinator<C> build();
   }
}
