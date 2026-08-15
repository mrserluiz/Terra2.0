package org.incendo.cloud.type;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "Either", generator = "Immutables")
@Immutable
final class EitherImpl<U, V> implements Either<U, V> {
   @Nullable
   private final U primary;
   @Nullable
   private final V fallback;

   private EitherImpl(Optional<? extends U> primary, Optional<? extends V> fallback) {
      this.primary = (U)primary.orElse(null);
      this.fallback = (V)fallback.orElse(null);
   }

   private EitherImpl(@Nullable U primary, @Nullable V fallback) {
      this.primary = primary;
      this.fallback = fallback;
   }

   private EitherImpl(EitherImpl<U, V> original, @Nullable U primary, @Nullable V fallback) {
      this.primary = primary;
      this.fallback = fallback;
   }

   @Override
   public @NonNull Optional<U> primary() {
      return Optional.ofNullable(this.primary);
   }

   @Override
   public @NonNull Optional<V> fallback() {
      return Optional.ofNullable(this.fallback);
   }

   public final EitherImpl<U, V> withPrimary(@Nullable U value) {
      U newValue = value;
      return this.primary == newValue ? this : new EitherImpl<>(this, newValue, this.fallback);
   }

   public final EitherImpl<U, V> withPrimary(Optional<? extends U> optional) {
      U value = (U)optional.orElse(null);
      return this.primary == value ? this : new EitherImpl<>(this, value, this.fallback);
   }

   public final EitherImpl<U, V> withFallback(@Nullable V value) {
      V newValue = value;
      return this.fallback == newValue ? this : new EitherImpl<>(this, this.primary, newValue);
   }

   public final EitherImpl<U, V> withFallback(Optional<? extends V> optional) {
      V value = (V)optional.orElse(null);
      return this.fallback == value ? this : new EitherImpl<>(this, this.primary, value);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof EitherImpl && this.equalTo(0, (EitherImpl<?, ?>)another);
   }

   private boolean equalTo(int synthetic, EitherImpl<?, ?> another) {
      return Objects.equals(this.primary, another.primary) && Objects.equals(this.fallback, another.fallback);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + Objects.hashCode(this.primary);
      return h + (h << 5) + Objects.hashCode(this.fallback);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder("Either{");
      if (this.primary != null) {
         builder.append("primary=").append(this.primary);
      }

      if (this.fallback != null) {
         if (builder.length() > 7) {
            builder.append(", ");
         }

         builder.append("fallback=").append(this.fallback);
      }

      return builder.append("}").toString();
   }

   public static <U, V> EitherImpl<U, V> of(Optional<? extends U> primary, Optional<? extends V> fallback) {
      return new EitherImpl<>(primary, fallback);
   }

   public static <U, V> EitherImpl<U, V> of(@Nullable U primary, @Nullable V fallback) {
      return new EitherImpl<>(primary, fallback);
   }

   public static <U, V> EitherImpl<U, V> copyOf(Either<U, V> instance) {
      return instance instanceof EitherImpl ? (EitherImpl)instance : of(instance.primary(), instance.fallback());
   }
}
