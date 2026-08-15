package org.incendo.cloud.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
final class AnnotatedElementAccessor implements AnnotationAccessor {
   private final AnnotatedElement element;

   AnnotatedElementAccessor(final @NonNull AnnotatedElement element) {
      this.element = Objects.requireNonNull(element, "Method may not be null");
   }

   @Override
   public <A extends Annotation> @Nullable A annotation(final @NonNull Class<A> clazz) {
      return this.element.getAnnotation(clazz);
   }

   @Override
   public @NonNull Collection<@NonNull Annotation> annotations() {
      return Collections.unmodifiableCollection(Arrays.asList(this.element.getAnnotations()));
   }
}
