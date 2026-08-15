package org.incendo.cloud.type;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface Either<U, V> {
   static <U, V> @NonNull Either<U, V> ofPrimary(final @NonNull U value) {
      return EitherImpl.of(Objects.requireNonNull(value, "value"), null);
   }

   static <U, V> @NonNull Either<U, V> ofFallback(final @NonNull V value) {
      return EitherImpl.of(null, Objects.requireNonNull(value, "value"));
   }

   @NonNull Optional<U> primary();

   @NonNull Optional<V> fallback();

   default @NonNull U primaryOrMapFallback(final @NonNull Function<V, U> mapFallback) {
      return this.primary().orElseGet(() -> mapFallback.apply(this.fallback().get()));
   }

   default @NonNull V fallbackOrMapPrimary(final @NonNull Function<U, V> mapPrimary) {
      return this.fallback().orElseGet(() -> mapPrimary.apply(this.primary().get()));
   }

   default <R> @NonNull R mapEither(final @NonNull Function<U, R> mapPrimary, final @NonNull Function<V, R> mapFallback) {
      return this.primary().map(mapPrimary).orElseGet(() -> this.fallback().map(mapFallback).get());
   }
}
