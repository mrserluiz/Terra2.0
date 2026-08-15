package org.incendo.cloud.suggestion;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

final class MappingSuggestionFactory<C, S extends Suggestion> implements SuggestionFactory<C, S> {
   private final SuggestionFactory<C, ?> other;
   private final SuggestionMapper<S> suggestionMapper;

   MappingSuggestionFactory(final @NonNull SuggestionFactory<C, ?> other, final @NonNull SuggestionMapper<S> suggestionMapper) {
      this.other = other;
      this.suggestionMapper = suggestionMapper;
   }

   @Override
   public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(final @NonNull CommandContext<C> context, final @NonNull String input) {
      return this.map(this.other.suggest(context, input));
   }

   @Override
   public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(final @NonNull C sender, final @NonNull String input) {
      return this.map(this.other.suggest(sender, input));
   }

   @Override
   public <S2 extends Suggestion> @NonNull SuggestionFactory<C, S2> mapped(final @NonNull SuggestionMapper<S2> mapper) {
      return new MappingSuggestionFactory<>(this.other, this.suggestionMapper.then(mapper));
   }

   private <S1 extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> map(final @NonNull CompletableFuture<Suggestions<C, S1>> future) {
      return future.thenApply(
         suggestions -> Suggestions.create(
            suggestions.commandContext(), suggestions.list().stream().map(this.suggestionMapper::map).collect(Collectors.toList()), suggestions.commandInput()
         )
      );
   }
}
