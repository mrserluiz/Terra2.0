package org.incendo.cloud.suggestion;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
public interface SuggestionFactory<C, S extends Suggestion> {
   @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(@NonNull CommandContext<C> context, @NonNull String input);

   @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(@NonNull C sender, @NonNull String input);

   default @NonNull Suggestions<C, S> suggestImmediately(final @NonNull C sender, final @NonNull String input) {
      try {
         return this.suggest(sender, input).join();
      } catch (CompletionException completionException) {
         Throwable cause = completionException.getCause();
         if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
         } else {
            throw completionException;
         }
      }
   }

   default <S2 extends Suggestion> @NonNull SuggestionFactory<C, S2> mapped(final @NonNull SuggestionMapper<S2> mapper) {
      return new MappingSuggestionFactory<>(this, mapper);
   }
}
