package org.incendo.cloud.description;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface Description {
   Description EMPTY = DescriptionImpl.of("");

   static @NonNull Description empty() {
      return EMPTY;
   }

   static @NonNull Description of(final @NonNull String string) {
      return Objects.requireNonNull(string, "string").isEmpty() ? empty() : DescriptionImpl.of(string);
   }

   static @NonNull Description description(final @NonNull String string) {
      return of(string);
   }

   @NonNull String textDescription();

   default boolean isEmpty() {
      return this.textDescription().isEmpty();
   }
}
