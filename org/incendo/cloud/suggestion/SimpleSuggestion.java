package org.incendo.cloud.suggestion;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

final class SimpleSuggestion implements Suggestion {
   private final String suggestion;

   SimpleSuggestion(final @NonNull String suggestion) {
      this.suggestion = suggestion;
   }

   @Override
   public @NonNull String suggestion() {
      return this.suggestion;
   }

   @Override
   public @NonNull Suggestion withSuggestion(final @NonNull String suggestion) {
      return new SimpleSuggestion(suggestion);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SimpleSuggestion that = (SimpleSuggestion)o;
         return Objects.equals(this.suggestion, that.suggestion);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.suggestion);
   }

   @Override
   public String toString() {
      return this.suggestion;
   }
}
