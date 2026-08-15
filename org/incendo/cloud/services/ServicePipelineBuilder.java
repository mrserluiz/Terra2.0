package org.incendo.cloud.services;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ServicePipelineBuilder {
   private Executor executor = Executors.newSingleThreadExecutor();

   ServicePipelineBuilder() {
   }

   public @NonNull ServicePipeline build() {
      return new ServicePipeline(this.executor);
   }

   public @NonNull ServicePipelineBuilder withExecutor(final @NonNull Executor executor) {
      this.executor = Objects.requireNonNull(executor, "Executor may not be null");
      return this;
   }
}
