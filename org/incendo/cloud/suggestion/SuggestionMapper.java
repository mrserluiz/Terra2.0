package org.incendo.cloud.suggestion;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(status = Status.STABLE)
public interface SuggestionMapper<S extends Suggestion> {
   static @NonNull SuggestionMapper<Suggestion> identity() {
      return suggestion -> suggestion;
   }

   @NonNull S map(@NonNull Suggestion suggestion);

   default <S1 extends Suggestion> @NonNull SuggestionMapper<S1> then(final @NonNull SuggestionMapper<S1> mapper) {
      Objects.requireNonNull(mapper, "mapper");
      return suggestion -> mapper.map(this.map(suggestion));
   }
}
