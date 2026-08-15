package org.incendo.cloud.suggestion;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@FunctionalInterface
@API(status = Status.STABLE)
public interface BlockingSuggestionProvider<C> extends SuggestionProvider<C> {
   @NonNull Iterable<? extends @NonNull Suggestion> suggestions(@NonNull CommandContext<C> context, @NonNull CommandInput input);

   @Override
   default @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
      final @NonNull CommandContext<C> context, final @NonNull CommandInput input
   ) {
      return CompletableFuture.completedFuture(this.suggestions(context, input));
   }

   @FunctionalInterface
   @API(status = Status.STABLE)
   interface Strings<C> extends BlockingSuggestionProvider<C> {
      @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<C> commandContext, @NonNull CommandInput input);

      @Override
      default @NonNull Iterable<@NonNull Suggestion> suggestions(final @NonNull CommandContext<C> context, final @NonNull CommandInput input) {
         return StreamSupport.stream(this.stringSuggestions(context, input).spliterator(), false).map(Suggestion::suggestion).collect(Collectors.toList());
      }
   }
}
