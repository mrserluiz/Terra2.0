package org.incendo.cloud.services;

import io.leangen.geantyref.TypeToken;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.type.ConsumerService;
import org.incendo.cloud.services.type.Service;
import org.incendo.cloud.services.type.SideEffectService;

public final class ServiceSpigot<Context, Result> {
   private final Context context;
   private final ServicePipeline pipeline;
   private final ServiceRepository<Context, Result> repository;

   ServiceSpigot(
      final @NonNull ServicePipeline pipeline,
      final @NonNull Context context,
      final @NonNull TypeToken<? extends Service<@NonNull Context, @NonNull Result>> type
   ) {
      this.context = context;
      this.pipeline = pipeline;
      this.repository = pipeline.getRepository(type);
   }

   public Result complete() throws IllegalStateException, PipelineException {
      LinkedList<? extends ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>> queue = this.repository.queue();
      queue.sort(null);
      boolean consumerService = false;

      ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>> wrapper;
      while ((wrapper = (ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>)queue.pollLast()) != null) {
         consumerService = wrapper.implementation() instanceof ConsumerService;
         if (ServiceFilterHandler.INSTANCE.passes(wrapper, this.context)) {
            Result result;
            try {
               result = wrapper.implementation().handle(this.context);
            } catch (Exception e) {
               throw new PipelineException(String.format("Failed to retrieve result from %s", wrapper), e);
            }

            if (wrapper.implementation() instanceof SideEffectService) {
               if (result == null) {
                  throw new IllegalStateException(String.format("SideEffectService '%s' returned null", wrapper));
               }

               if (result == State.ACCEPTED) {
                  return result;
               }
            } else if (result != null) {
               return result;
            }
         }
      }

      if (consumerService) {
         return (Result)State.ACCEPTED;
      } else {
         throw new IllegalStateException("No service consumed the context. This means that the pipeline was not constructed properly.");
      }
   }

   public void complete(final @NonNull BiConsumer<Result, Throwable> consumer) {
      try {
         consumer.accept(this.complete(), null);
      } catch (PipelineException pipelineException) {
         consumer.accept(null, pipelineException.getCause());
      } catch (Exception e) {
         consumer.accept(null, e);
      }
   }

   public @NonNull CompletableFuture<Result> completeAsynchronously() {
      return CompletableFuture.supplyAsync(this::complete, this.pipeline.executor());
   }

   public @NonNull ServicePump<Result> forward() {
      return this.pipeline.pump(this.complete());
   }

   public @NonNull CompletableFuture<ServicePump<Result>> forwardAsynchronously() {
      return this.completeAsynchronously().thenApply(this.pipeline::pump);
   }
}
