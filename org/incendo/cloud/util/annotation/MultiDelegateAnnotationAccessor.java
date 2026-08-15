package org.incendo.cloud.util.annotation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
final class MultiDelegateAnnotationAccessor implements AnnotationAccessor {
   private final AnnotationAccessor[] accessors;

   MultiDelegateAnnotationAccessor(final @NonNull AnnotationAccessor @NonNull ... accessors) {
      this.accessors = accessors;
   }

   @Override
   public <A extends Annotation> @Nullable A annotation(final @NonNull Class<A> clazz) {
      A instance = null;

      for (AnnotationAccessor annotationAccessor : this.accessors) {
         instance = annotationAccessor.annotation(clazz);
         if (instance != null) {
            break;
         }
      }

      return instance;
   }

   @Override
   public @NonNull Collection<@NonNull Annotation> annotations() {
      List<Annotation> annotationList = new LinkedList<>();

      for (AnnotationAccessor annotationAccessor : this.accessors) {
         annotationList.addAll(annotationAccessor.annotations());
      }

      return Collections.unmodifiableCollection(annotationList);
   }
}
