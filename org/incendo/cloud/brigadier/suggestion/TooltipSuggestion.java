package org.incendo.cloud.brigadier.suggestion;

import com.mojang.brigadier.Message;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.suggestion.Suggestion;

@API(status = Status.STABLE, since = "2.0.0")
@Immutable
public interface TooltipSuggestion extends Suggestion {
   static @NonNull TooltipSuggestion suggestion(final @NonNull String suggestion, final @Nullable Message tooltip) {
      return TooltipSuggestionImpl.of(suggestion, tooltip);
   }

   static @NonNull TooltipSuggestion tooltipSuggestion(final @NonNull Suggestion suggestion) {
      return suggestion instanceof TooltipSuggestion ? (TooltipSuggestion)suggestion : suggestion(suggestion.suggestion(), null);
   }

   @Override
   @NonNull String suggestion();

   @Nullable Message tooltip();

   @NonNull TooltipSuggestion withSuggestion(@NonNull String suggestion);
}
