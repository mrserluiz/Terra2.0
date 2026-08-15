package org.incendo.cloud.services;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.type.Service;

public final class ServicePipeline {
   private final Object lock = new Object();
   private final Map<Type, ServiceRepository<?, ?>> repositories = new HashMap<>();
   private final Executor executor;

   ServicePipeline(final @NonNull Executor executor) {
      this.executor = executor;
   }

   public static @NonNull ServicePipelineBuilder builder() {
      return new ServicePipelineBuilder();
   }

   public <Context, Result> @NonNull ServicePipeline registerServiceType(
      final @NonNull TypeToken<? extends Service<@NonNull Context, @NonNull Result>> type,
      final @NonNull Service<@NonNull Context, @NonNull Result> defaultImplementation
   ) {
      synchronized (this.lock) {
         if (this.repositories.containsKey(type.getType())) {
            throw new IllegalArgumentException(String.format("Service of type '%s' has already been registered", type.getType().getTypeName()));
         }

         ServiceRepository<Context, Result> repository = new ServiceRepository<>(type);
         repository.registerImplementation(defaultImplementation, Collections.emptyList());
         this.repositories.put(type.getType(), repository);
         return this;
      }
   }

   public <T> @NonNull ServicePipeline registerMethods(final @NonNull T instance) throws Exception {
      synchronized (this.lock) {
         Map<? extends Service<?, ?>, TypeToken<? extends Service<?, ?>>> services = AnnotatedMethodServiceFactory.INSTANCE.lookupServices(instance);

         for (Entry<? extends Service<?, ?>, TypeToken<? extends Service<?, ?>>> serviceEntry : services.entrySet()) {
            TypeToken<? extends Service<?, ?>> type = serviceEntry.getValue();
            ServiceRepository<?, ?> repository = this.repositories.get(type.getType());
            if (repository == null) {
               throw new IllegalArgumentException(String.format("No service registered for type '%s'", type.getType().getTypeName()));
            }

            repository.registerImplementation((T)((Service)serviceEntry.getKey()), Collections.emptyList());
         }

         return this;
      }
   }

   public <Context, Result> ServicePipeline registerServiceImplementation(
      final @NonNull TypeToken<? extends Service<Context, Result>> type,
      final @NonNull Service<Context, Result> implementation,
      final @NonNull Collection<Predicate<Context>> filters
   ) {
      synchronized (this.lock) {
         ServiceRepository<Context, Result> repository = this.getRepository(type);
         repository.registerImplementation(implementation, filters);
         return this;
      }
   }

   public <Context, Result> ServicePipeline registerServiceImplementation(
      final @NonNull Class<? extends Service<Context, Result>> type,
      final @NonNull Service<Context, Result> implementation,
      final @NonNull Collection<Predicate<Context>> filters
   ) {
      return this.registerServiceImplementation(TypeToken.get(type), implementation, filters);
   }

   public <Context> @NonNull ServicePump<Context> pump(final @NonNull Context context) {
      return new ServicePump<>(this, context);
   }

   <Context, Result> @NonNull ServiceRepository<Context, Result> getRepository(final @NonNull TypeToken<? extends Service<Context, Result>> type) {
      ServiceRepository<Context, Result> repository = (ServiceRepository<Context, Result>)this.repositories.get(type.getType());
      if (repository == null) {
         throw new IllegalArgumentException(String.format("No service registered for type '%s'", type.getType().getTypeName()));
      } else {
         return repository;
      }
   }

   public @NonNull Collection<Type> recognizedTypes() {
      return Collections.unmodifiableCollection(this.repositories.keySet());
   }

   public <Context, Result, S extends Service<Context, Result>> @NonNull Collection<TypeToken<? extends S>> getImplementations(final @NonNull TypeToken<S> type) {
      ServiceRepository<Context, Result> repository = this.getRepository(type);
      List<TypeToken<? extends S>> collection = new LinkedList<>();
      LinkedList<? extends ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>> queue = repository.queue();
      queue.sort(null);
      Collections.reverse(queue);

      for (ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>> wrapper : queue) {
         collection.add(TypeToken.get((Class<? extends S>)wrapper.implementation().getClass()));
      }

      return Collections.unmodifiableList(collection);
   }

   @NonNull Executor executor() {
      return this.executor;
   }
}
