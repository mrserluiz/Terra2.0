package org.incendo.cloud.exception.handling;

import io.leangen.geantyref.TypeToken;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public final class ExceptionHandlerRegistration<C, T extends Throwable> {
   private final TypeToken<T> exceptionType;
   private final ExceptionHandler<C, ? extends T> exceptionHandler;
   private final Predicate<T> exceptionFilter;

   public static <C, T extends Throwable> @NonNull ExceptionHandlerRegistration<C, ? extends T> of(
      final @NonNull TypeToken<T> exceptionType, final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
   ) {
      return builder(exceptionType).exceptionHandler(exceptionHandler).build();
   }

   public static <C, T extends Throwable> ExceptionHandlerRegistration.@NonNull ExceptionControllerBuilder<C, T> builder(
      final @NonNull TypeToken<T> exceptionType
   ) {
      return new ExceptionHandlerRegistration.ExceptionControllerBuilder<>(exceptionType);
   }

   private ExceptionHandlerRegistration(
      final @NonNull TypeToken<T> exceptionType, final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler, final @NonNull Predicate<T> exceptionFilter
   ) {
      this.exceptionType = exceptionType;
      this.exceptionHandler = exceptionHandler;
      this.exceptionFilter = exceptionFilter;
   }

   public @NonNull TypeToken<T> exceptionType() {
      return this.exceptionType;
   }

   public @NonNull ExceptionHandler<C, ? extends T> exceptionHandler() {
      return this.exceptionHandler;
   }

   public @NonNull Predicate<T> exceptionFilter() {
      return this.exceptionFilter;
   }

   @FunctionalInterface
   @API(status = Status.STABLE)
   public interface BuilderDecorator<C, T extends Throwable> {
      ExceptionHandlerRegistration.@NonNull ExceptionControllerBuilder<C, T> decorate(
         ExceptionHandlerRegistration.@NonNull ExceptionControllerBuilder<C, T> builder
      );
   }

   @API(status = Status.STABLE)
   public static final class ExceptionControllerBuilder<C, T extends Throwable> {
      private final TypeToken<T> exceptionType;
      private final ExceptionHandler<C, ? extends T> exceptionHandler;
      private final Predicate<T> exceptionFilter;

      private ExceptionControllerBuilder(
         final @NonNull TypeToken<T> exceptionType,
         final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler,
         final @NonNull Predicate<T> exceptionFilter
      ) {
         this.exceptionType = exceptionType;
         this.exceptionHandler = exceptionHandler;
         this.exceptionFilter = exceptionFilter;
      }

      private ExceptionControllerBuilder(final @NonNull TypeToken<T> exceptionType) {
         this(exceptionType, ExceptionHandler.noopHandler(), exception -> true);
      }

      public ExceptionHandlerRegistration.@NonNull ExceptionControllerBuilder<C, T> exceptionHandler(
         final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
      ) {
         return new ExceptionHandlerRegistration.ExceptionControllerBuilder<>(this.exceptionType, exceptionHandler, this.exceptionFilter);
      }

      public ExceptionHandlerRegistration.@NonNull ExceptionControllerBuilder<C, T> exceptionFilter(final @NonNull Predicate<T> exceptionFilter) {
         return new ExceptionHandlerRegistration.ExceptionControllerBuilder<>(this.exceptionType, this.exceptionHandler, exceptionFilter);
      }

      public @NonNull ExceptionHandlerRegistration<C, ? extends T> build() {
         return new ExceptionHandlerRegistration<>(this.exceptionType, this.exceptionHandler, this.exceptionFilter);
      }
   }
}
