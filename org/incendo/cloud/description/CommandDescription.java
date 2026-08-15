package org.incendo.cloud.description;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface CommandDescription extends Describable {
   static @NonNull CommandDescription empty() {
      return CommandDescriptionImpl.of(Description.empty(), Description.empty());
   }

   static @NonNull CommandDescription commandDescription(final @NonNull Description description, final @NonNull Description verboseDescription) {
      return CommandDescriptionImpl.of(description, verboseDescription);
   }

   static @NonNull CommandDescription commandDescription(final @NonNull Description description) {
      return CommandDescriptionImpl.of(description, description);
   }

   static @NonNull CommandDescription commandDescription(final @NonNull String description, final @NonNull String verboseDescription) {
      return CommandDescriptionImpl.of(Description.of(description), Description.of(verboseDescription));
   }

   static @NonNull CommandDescription commandDescription(final @NonNull String description) {
      return CommandDescriptionImpl.of(Description.of(description), Description.of(description));
   }

   @Override
   @NonNull Description description();

   @NonNull Description verboseDescription();

   default boolean isEmpty() {
      return this.description().equals(Description.empty());
   }
}
