package org.incendo.cloud.suggestion;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

final class NoSuggestions implements SuggestionProvider<Object> {
   private static final SuggestionProvider<?> INSTANCE = new NoSuggestions();
   private final CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> result = CompletableFuture.completedFuture(
      Collections.emptyList()
   );

   private NoSuggestions() {
   }

   @Override
   public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
      final @NonNull CommandContext<Object> context, final @NonNull CommandInput input
   ) {
      return this.result;
   }

   static <C> @NonNull SuggestionProvider<C> instance() {
      return (SuggestionProvider<C>)INSTANCE;
   }
}
