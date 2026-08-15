package org.incendo.cloud.suggestion;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@API(status = Status.STABLE)
@FunctionalInterface
public interface SuggestionProvider<C> {
   @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
      @NonNull CommandContext<C> context, @NonNull CommandInput input
   );

   static <C> @NonNull SuggestionProvider<C> noSuggestions() {
      return NoSuggestions.instance();
   }

   static <C> @NonNull SuggestionProvider<C> blocking(final @NonNull BlockingSuggestionProvider<C> blockingSuggestionProvider) {
      return blockingSuggestionProvider;
   }

   static <C> @NonNull SuggestionProvider<C> blockingStrings(final BlockingSuggestionProvider.@NonNull Strings<C> blockingStringsSuggestionProvider) {
      return blockingStringsSuggestionProvider;
   }

   static <C> @NonNull SuggestionProvider<C> suggesting(final @NonNull Suggestion @NonNull ... suggestions) {
      return suggesting(Arrays.asList(suggestions));
   }

   static <C> @NonNull SuggestionProvider<C> suggestingStrings(final @NonNull String @NonNull ... suggestions) {
      return suggestingStrings(Arrays.asList(suggestions));
   }

   static <C> @NonNull SuggestionProvider<C> suggesting(final @NonNull Iterable<? extends @NonNull Suggestion> suggestions) {
      return blocking((ctx, input) -> suggestions);
   }

   static <C> @NonNull SuggestionProvider<C> suggestingStrings(final @NonNull Iterable<@NonNull String> suggestions) {
      return blockingStrings((ctx, input) -> suggestions);
   }
}
