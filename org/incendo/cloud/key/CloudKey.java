package org.incendo.cloud.key;

import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public abstract class CloudKey<T> {
   @API(status = Status.STABLE)
   public static <T> CloudKey<T> of(final @NonNull String name, final @NonNull TypeToken<T> type) {
      return CloudKeyImpl.of(name, type);
   }

   @API(status = Status.STABLE)
   public static <T> CloudKey<T> of(final @NonNull String name, final @NonNull Class<T> type) {
      return CloudKeyImpl.of(name, TypeToken.get(type));
   }

   @API(status = Status.STABLE)
   public static @NonNull CloudKey<Void> of(final @NonNull String name) {
      return CloudKeyImpl.of(name, TypeToken.get(void.class));
   }

   @API(status = Status.STABLE)
   public static <T> CloudKey<T> cloudKey(final @NonNull String name, final @NonNull TypeToken<T> type) {
      return CloudKeyImpl.of(name, type);
   }

   @API(status = Status.STABLE)
   public static <T> CloudKey<T> cloudKey(final @NonNull String name, final @NonNull Class<T> type) {
      return CloudKeyImpl.of(name, TypeToken.get(type));
   }

   @API(status = Status.STABLE)
   public static @NonNull CloudKey<Void> cloudKey(final @NonNull String name) {
      return CloudKeyImpl.of(name, TypeToken.get(void.class));
   }

   public abstract @NonNull String name();

   public abstract @NonNull TypeToken<@NonNull T> type();

   @Override
   public final boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         CloudKey<?> that = (CloudKey<?>)other;
         return Objects.equals(this.name(), that.name());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hashCode(this.name());
   }
}
