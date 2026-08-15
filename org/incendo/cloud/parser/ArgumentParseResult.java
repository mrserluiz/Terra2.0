package org.incendo.cloud.parser;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.exception.handling.ExceptionController;

@API(status = Status.STABLE)
public abstract class ArgumentParseResult<T> {
   private ArgumentParseResult() {
   }

   public static <T> @NonNull ArgumentParseResult<T> failure(final @NonNull Throwable failure) {
      return new ArgumentParseResult.ParseFailure<>(failure);
   }

   @API(status = Status.STABLE)
   public static <T> @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> failureFuture(final @NonNull Throwable failure) {
      return new ArgumentParseResult.ParseFailure<T>(failure).asFuture();
   }

   public static <T> @NonNull ArgumentParseResult<T> success(final @NonNull T value) {
      return new ArgumentParseResult.ParseSuccess<>(value);
   }

   @API(status = Status.STABLE)
   public static <T> @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> successFuture(final @NonNull T value) {
      return success(value).asFuture();
   }

   @API(status = Status.STABLE)
   public abstract @NonNull Optional<T> parsedValue();

   @API(status = Status.STABLE)
   public abstract @NonNull Optional<Throwable> failure();

   @API(status = Status.STABLE)
   public final @NonNull CompletableFuture<ArgumentParseResult<T>> asFuture() {
      return CompletableFuture.completedFuture(this);
   }

   public abstract <O> @NonNull CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(
      @NonNull Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper
   );

   public abstract <O> @NonNull CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(@NonNull Function<T, CompletableFuture<O>> mapper);

   public abstract <O> @NonNull ArgumentParseResult<O> flatMapSuccess(@NonNull Function<T, ArgumentParseResult<O>> mapper);

   public abstract <O> @NonNull ArgumentParseResult<O> mapSuccess(@NonNull Function<T, O> mapper);

   private static final class ParseFailure<T> extends ArgumentParseResult<T> {
      private final Throwable failure;

      private ParseFailure(final @NonNull Throwable failure) {
         this.failure = ExceptionController.unwrapCompletionException(failure);
      }

      @Override
      public @NonNull Optional<T> parsedValue() {
         return Optional.empty();
      }

      @Override
      public @NonNull Optional<Throwable> failure() {
         return Optional.of(this.failure);
      }

      @Override
      public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(
         final @NonNull Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper
      ) {
         return CompletableFuture.completedFuture(this.self());
      }

      @Override
      public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(final @NonNull Function<T, CompletableFuture<O>> mapper) {
         return CompletableFuture.completedFuture(this.self());
      }

      @Override
      public <O> @NonNull ArgumentParseResult<O> flatMapSuccess(final @NonNull Function<T, ArgumentParseResult<O>> mapper) {
         return this.self();
      }

      @Override
      public <O> @NonNull ArgumentParseResult<O> mapSuccess(final @NonNull Function<T, O> mapper) {
         return this.self();
      }

      private <O> @NonNull ArgumentParseResult<O> self() {
         return (ArgumentParseResult<O>)this;
      }
   }

   private static final class ParseSuccess<T> extends ArgumentParseResult<T> {
      private final T value;

      private ParseSuccess(final @NonNull T value) {
         this.value = value;
      }

      @Override
      public @NonNull Optional<T> parsedValue() {
         return Optional.of(this.value);
      }

      @Override
      public @NonNull Optional<Throwable> failure() {
         return Optional.empty();
      }

      @Override
      public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(
         final @NonNull Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper
      ) {
         return mapper.apply(this.value);
      }

      @Override
      public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(final @NonNull Function<T, CompletableFuture<O>> mapper) {
         return mapper.apply(this.value).thenApply(ArgumentParseResult::success);
      }

      @Override
      public <O> @NonNull ArgumentParseResult<O> flatMapSuccess(final @NonNull Function<T, ArgumentParseResult<O>> mapper) {
         return mapper.apply(this.value);
      }

      @Override
      public <O> @NonNull ArgumentParseResult<O> mapSuccess(final @NonNull Function<T, O> mapper) {
         return ArgumentParseResult.success(mapper.apply(this.value));
      }
   }
}
