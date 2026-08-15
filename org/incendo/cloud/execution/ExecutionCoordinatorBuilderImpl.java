package org.incendo.cloud.execution;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
final class ExecutionCoordinatorBuilderImpl<C> implements ExecutionCoordinator.Builder<C> {
   private @Nullable Executor parsingExecutor;
   private @Nullable Executor suggestionsExecutor;
   private @Nullable Executor executionSchedulingExecutor;
   private boolean synchronizeExecution = false;

   @Override
   public ExecutionCoordinator.@NonNull Builder<C> parsingExecutor(final @NonNull Executor executor) {
      Objects.requireNonNull(executor, "executor");
      this.parsingExecutor = executor;
      return this;
   }

   @Override
   public ExecutionCoordinator.@NonNull Builder<C> suggestionsExecutor(final @NonNull Executor executor) {
      Objects.requireNonNull(executor, "executor");
      this.suggestionsExecutor = executor;
      return this;
   }

   @Override
   public ExecutionCoordinator.@NonNull Builder<C> executionSchedulingExecutor(final @NonNull Executor executor) {
      Objects.requireNonNull(executor, "executor");
      this.executionSchedulingExecutor = executor;
      return this;
   }

   @Override
   public ExecutionCoordinator.@NonNull Builder<C> synchronizeExecution(final boolean synchronizeExecution) {
      this.synchronizeExecution = synchronizeExecution;
      return this;
   }

   @Override
   public @NonNull ExecutionCoordinator<C> build() {
      return new ExecutionCoordinatorImpl<>(this.parsingExecutor, this.suggestionsExecutor, this.executionSchedulingExecutor, this.synchronizeExecution);
   }
}
