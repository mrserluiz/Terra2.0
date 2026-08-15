package org.incendo.cloud.injection;

import com.google.inject.BindingAnnotation;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import java.lang.annotation.Annotation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

@API(status = Status.STABLE)
public final class GuiceInjectionService<C> implements InjectionService<C> {
   private final Injector injector;

   private static <T> @NonNull Key<T> createKey(final @NonNull Class<T> clazz, final @NonNull AnnotationAccessor annotationAccessor) {
      Annotation bindingAnnotation = annotationAccessor.annotations()
         .stream()
         .filter(annotation -> annotation.annotationType().isAnnotationPresent(BindingAnnotation.class))
         .findFirst()
         .orElse(null);
      return bindingAnnotation == null ? Key.get(clazz) : Key.get(clazz, bindingAnnotation);
   }

   private GuiceInjectionService(final @NonNull Injector injector) {
      this.injector = injector;
   }

   public static <C> @NonNull GuiceInjectionService<C> create(final @NonNull Injector injector) {
      return new GuiceInjectionService<>(injector);
   }

   public @Nullable Object handle(final @NonNull InjectionRequest<C> request) {
      try {
         return this.injector.getInstance(createKey(request.injectedClass(), request.annotationAccessor()));
      } catch (ConfigurationException ignored) {
         return null;
      }
   }
}
