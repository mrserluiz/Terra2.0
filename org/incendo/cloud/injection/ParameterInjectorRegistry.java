package org.incendo.cloud.injection;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.InjectionException;
import org.incendo.cloud.services.ServicePipeline;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

@API(status = Status.STABLE)
public final class ParameterInjectorRegistry<C> implements InjectionService<C> {
   private final List<Pair<Predicate<TypeToken<?>>, ParameterInjector<C, ?>>> injectors = new ArrayList<>();
   private final ServicePipeline servicePipeline = ServicePipeline.builder().build();

   public ParameterInjectorRegistry() {
      this.servicePipeline.registerServiceType(new TypeToken<InjectionService<C>>() {}, this);
   }

   public synchronized <T> @This @NonNull ParameterInjectorRegistry<C> registerInjector(
      final @NonNull Class<T> clazz, final @NonNull ParameterInjector<C, T> injector
   ) {
      return this.registerInjector(TypeToken.get(clazz), injector);
   }

   @API(status = Status.STABLE)
   public synchronized <T> @This @NonNull ParameterInjectorRegistry<C> registerInjector(
      final @NonNull TypeToken<T> type, final @NonNull ParameterInjector<C, T> injector
   ) {
      return this.registerInjector(cl -> GenericTypeReflector.isSuperType(cl.getType(), type.getType()), injector);
   }

   @API(status = Status.STABLE)
   public synchronized <T> @This @NonNull ParameterInjectorRegistry<C> registerInjector(
      final @NonNull Predicate<TypeToken<?>> predicate, final @NonNull ParameterInjector<C, T> injector
   ) {
      this.injectors.add(Pair.of(predicate, injector));
      return this;
   }

   public @Nullable Object handle(final @NonNull InjectionRequest<C> request) {
      for (ParameterInjector<C, ?> injector : this.injectors(request.injectedType())) {
         Object value = injector.create(request.commandContext(), request.annotationAccessor());
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Optional<T> getInjectable(
      final @NonNull Class<T> clazz, final @NonNull CommandContext<C> context, final @NonNull AnnotationAccessor annotationAccessor
   ) {
      return this.getInjectable(TypeToken.get(clazz), context, annotationAccessor);
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Optional<T> getInjectable(
      final @NonNull TypeToken<T> type, final @NonNull CommandContext<C> context, final @NonNull AnnotationAccessor annotationAccessor
   ) {
      InjectionRequest<C> request = InjectionRequest.of(context, type, annotationAccessor);

      try {
         Object rawResult = this.servicePipeline.pump(request).through(new TypeToken<InjectionService<C>>() {}).complete();
         if (!request.injectedClass().isInstance(rawResult)) {
            throw new IllegalStateException(
               String.format("Injector returned type %s which is not an instance of %s", rawResult.getClass().getName(), request.injectedClass().getName())
            );
         }

         T result = (T)rawResult;
         return Optional.of(result);
      } catch (IllegalStateException ignored) {
         return Optional.empty();
      } catch (InjectionException injectionException) {
         throw injectionException;
      } catch (Exception e) {
         throw new InjectionException(String.format("Failed to inject type %s", type.getType().getTypeName()), e);
      }
   }

   @API(status = Status.STABLE)
   public @This @NonNull ParameterInjectorRegistry<C> registerInjectionService(final InjectionService<C> service) {
      this.servicePipeline.registerServiceImplementation(new TypeToken<InjectionService<C>>() {}, service, Collections.emptyList());
      return this;
   }

   private synchronized <T> @NonNull Collection<@NonNull ParameterInjector<C, ?>> injectors(final @NonNull TypeToken<T> type) {
      return Collections.unmodifiableCollection(this.injectors.stream().filter(pair -> pair.first().test(type)).map(Pair::second).collect(Collectors.toList()));
   }
}
