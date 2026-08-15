package org.incendo.cloud.suggestion;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface Suggestion {
   static @NonNull Suggestion suggestion(final @NonNull String suggestion) {
      return new SimpleSuggestion(suggestion);
   }

   @NonNull String suggestion();

   @NonNull Suggestion withSuggestion(@NonNull String suggestion);
}
