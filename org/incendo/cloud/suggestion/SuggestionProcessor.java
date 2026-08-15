package org.incendo.cloud.suggestion;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;

@FunctionalInterface
@API(status = Status.STABLE)
public interface SuggestionProcessor<C> {
   static <C> @NonNull SuggestionProcessor<C> passThrough() {
      return (ctx, suggestions) -> suggestions;
   }

   @NonNull Stream<@NonNull Suggestion> process(@NonNull CommandPreprocessingContext<C> context, @NonNull Stream<@NonNull Suggestion> suggestions);

   default @NonNull SuggestionProcessor<C> then(final @NonNull SuggestionProcessor<C> nextProcessor) {
      Objects.requireNonNull(nextProcessor, "nextProcessor");
      return new ChainedSuggestionProcessor<>(Arrays.asList(this, nextProcessor));
   }
}
