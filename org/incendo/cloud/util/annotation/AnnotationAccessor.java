package org.incendo.cloud.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.STABLE)
public interface AnnotationAccessor {
   @API(status = Status.STABLE)
   static @NonNull AnnotationAccessor empty() {
      return new AnnotationAccessor.NullAnnotationAccessor();
   }

   static @NonNull AnnotationAccessor of(final @NonNull AnnotatedElement element) {
      return new AnnotatedElementAccessor(element);
   }

   @API(status = Status.STABLE)
   static @NonNull AnnotationAccessor of(final @NonNull AnnotationAccessor @NonNull ... accessors) {
      return new MultiDelegateAnnotationAccessor(accessors);
   }

   <A extends Annotation> @Nullable A annotation(@NonNull Class<A> clazz);

   @NonNull Collection<@NonNull Annotation> annotations();

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   final class NullAnnotationAccessor implements AnnotationAccessor {
      @Override
      public <A extends Annotation> @Nullable A annotation(final @NonNull Class<A> clazz) {
         return null;
      }

      @Override
      public @NonNull Collection<@NonNull Annotation> annotations() {
         return Collections.emptyList();
      }
   }
}
