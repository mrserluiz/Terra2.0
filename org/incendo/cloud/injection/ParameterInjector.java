package org.incendo.cloud.injection;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

@FunctionalInterface
@API(status = Status.STABLE)
public interface ParameterInjector<C, T> {
   @API(status = Status.STABLE)
   static <C, T> @NonNull ParameterInjector<C, T> constantInjector(final @NonNull T value) {
      return new ParameterInjector.ConstantInjector<>(value);
   }

   @Nullable T create(@NonNull CommandContext<C> context, @NonNull AnnotationAccessor annotationAccessor);

   final class ConstantInjector<C, T> implements ParameterInjector<C, T> {
      private final T value;

      private ConstantInjector(final @NonNull T value) {
         this.value = value;
      }

      @Override
      public @NonNull T create(final @NonNull CommandContext<C> context, final @NonNull AnnotationAccessor annotationAccessor) {
         return this.value;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ParameterInjector.ConstantInjector<?, ?> that = (ParameterInjector.ConstantInjector<?, ?>)o;
            return Objects.equals(this.value, that.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.value);
      }

      @Override
      public String toString() {
         return "ConstantInjector{value=" + this.value + '}';
      }
   }
}
