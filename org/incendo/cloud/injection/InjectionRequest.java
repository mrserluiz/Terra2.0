package org.incendo.cloud.injection;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

@API(status = Status.STABLE)
@Immutable
public interface InjectionRequest<C> {
   static <C> @NonNull InjectionRequest<C> of(
      final @NonNull CommandContext<C> context, final @NonNull TypeToken<?> injectedType, final @NonNull AnnotationAccessor annotationAccessor
   ) {
      return InjectionRequestImpl.of(context, injectedType, annotationAccessor);
   }

   static <C> @NonNull InjectionRequest<C> of(final @NonNull CommandContext<C> context, final @NonNull TypeToken<?> injectedType) {
      return InjectionRequestImpl.of(context, injectedType, AnnotationAccessor.empty());
   }

   @NonNull CommandContext<C> commandContext();

   @NonNull TypeToken<?> injectedType();

   @Derived
   default @NonNull Class<?> injectedClass() {
      return GenericTypeReflector.erase(this.injectedType().getType());
   }

   @NonNull AnnotationAccessor annotationAccessor();
}
