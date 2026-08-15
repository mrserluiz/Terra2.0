package org.incendo.cloud.component;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;

@API(status = Status.STABLE)
@FunctionalInterface
public interface DefaultValue<C, T> {
   static <C, T> @NonNull DefaultValue<C, T> constant(final @NonNull T value) {
      return new DefaultValue.ConstantDefaultValue<>(Objects.requireNonNull(value, "value"));
   }

   static <C, T> @NonNull DefaultValue<C, T> dynamic(final DefaultValue.@NonNull DefaultValueProvider<C, T> expression) {
      Objects.requireNonNull(expression, "expression");
      return failableDynamic(ctx -> ArgumentParseResult.success(expression.evaluateDefault(ctx)));
   }

   static <C, T> @NonNull DefaultValue<C, T> failableDynamic(final @NonNull DefaultValue<C, T> expression) {
      return new DefaultValue.DynamicDefaultValue<>(Objects.requireNonNull(expression, "expression"));
   }

   static <C, T> @NonNull DefaultValue<C, T> parsed(final @NonNull String value) {
      return new DefaultValue.ParsedDefaultValue<>(value);
   }

   @NonNull ArgumentParseResult<T> evaluateDefault(@NonNull CommandContext<C> context);

   final class ConstantDefaultValue<C, T> implements DefaultValue<C, T> {
      private final ArgumentParseResult<T> value;

      private ConstantDefaultValue(final @NonNull T value) {
         this.value = ArgumentParseResult.success(value);
      }

      @Override
      public @NonNull ArgumentParseResult<T> evaluateDefault(final @NonNull CommandContext<C> context) {
         return this.value;
      }

      @Override
      public boolean equals(final Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            DefaultValue.ConstantDefaultValue<?, ?> that = (DefaultValue.ConstantDefaultValue<?, ?>)object;
            return Objects.equals(this.value.parsedValue().get(), that.value.parsedValue().get());
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.value);
      }
   }

   @API(status = Status.STABLE)
   @FunctionalInterface
   interface DefaultValueProvider<C, T> {
      @NonNull T evaluateDefault(@NonNull CommandContext<C> context);
   }

   final class DynamicDefaultValue<C, T> implements DefaultValue<C, T> {
      private final DefaultValue<C, T> defaultValue;

      private DynamicDefaultValue(final @NonNull DefaultValue<C, T> defaultValue) {
         this.defaultValue = defaultValue;
      }

      @Override
      public @NonNull ArgumentParseResult<T> evaluateDefault(final @NonNull CommandContext<C> context) {
         return this.defaultValue.evaluateDefault(context);
      }

      @Override
      public boolean equals(final Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            DefaultValue.DynamicDefaultValue<?, ?> that = (DefaultValue.DynamicDefaultValue<?, ?>)object;
            return Objects.equals(this.defaultValue, that.defaultValue);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.defaultValue);
      }
   }

   final class ParsedDefaultValue<C, T> implements DefaultValue<C, T> {
      private final String value;

      private ParsedDefaultValue(final @NonNull String string) {
         this.value = string;
      }

      @Override
      public @NonNull ArgumentParseResult<T> evaluateDefault(final @NonNull CommandContext<C> context) {
         throw new UnsupportedOperationException();
      }

      public @NonNull String value() {
         return this.value;
      }

      @Override
      public boolean equals(final Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            DefaultValue.ParsedDefaultValue<?, ?> that = (DefaultValue.ParsedDefaultValue<?, ?>)object;
            return Objects.equals(this.value, that.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.value);
      }
   }
}
