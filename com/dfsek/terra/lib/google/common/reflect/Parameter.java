package com.dfsek.terra.lib.google.common.reflect;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.FluentIterable;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class Parameter implements AnnotatedElement {
   private final Invokable<?, ?> declaration;
   private final int position;
   private final TypeToken<?> type;
   private final ImmutableList<Annotation> annotations;
   private final @Nullable Object annotatedType;

   Parameter(Invokable<?, ?> declaration, int position, TypeToken<?> type, Annotation[] annotations, @Nullable Object annotatedType) {
      this.declaration = declaration;
      this.position = position;
      this.type = type;
      this.annotations = ImmutableList.copyOf(annotations);
      this.annotatedType = annotatedType;
   }

   public TypeToken<?> getType() {
      return this.type;
   }

   public Invokable<?, ?> getDeclaringInvokable() {
      return this.declaration;
   }

   @Override
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
      return this.getAnnotation(annotationType) != null;
   }

   @Override
   public <A extends Annotation> @Nullable A getAnnotation(Class<A> annotationType) {
      Preconditions.checkNotNull(annotationType);

      for (Annotation annotation : this.annotations) {
         if (annotationType.isInstance(annotation)) {
            return annotationType.cast(annotation);
         }
      }

      return null;
   }

   @Override
   public Annotation[] getAnnotations() {
      return this.getDeclaredAnnotations();
   }

   @Override
   public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return this.getDeclaredAnnotationsByType(annotationType);
   }

   @Override
   public Annotation[] getDeclaredAnnotations() {
      return this.annotations.toArray(new Annotation[0]);
   }

   @Override
   public <A extends Annotation> @Nullable A getDeclaredAnnotation(Class<A> annotationType) {
      Preconditions.checkNotNull(annotationType);
      return FluentIterable.from(this.annotations).filter(annotationType).first().orNull();
   }

   @Override
   public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationType) {
      return FluentIterable.from(this.annotations).filter(annotationType).toArray(annotationType);
   }

   public AnnotatedType getAnnotatedType() {
      return Objects.requireNonNull((AnnotatedType)this.annotatedType);
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (!(obj instanceof Parameter)) {
         return false;
      }

      Parameter that = (Parameter)obj;
      return this.position == that.position && this.declaration.equals(that.declaration);
   }

   @Override
   public int hashCode() {
      return this.position;
   }

   @Override
   public String toString() {
      return this.type + " arg" + this.position;
   }
}
