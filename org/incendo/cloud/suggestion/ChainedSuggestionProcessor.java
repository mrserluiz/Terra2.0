package org.incendo.cloud.suggestion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;

@API(status = Status.INTERNAL)
final class ChainedSuggestionProcessor<C> implements SuggestionProcessor<C> {
   private final List<SuggestionProcessor<C>> links;

   ChainedSuggestionProcessor(final List<SuggestionProcessor<C>> links) {
      List<SuggestionProcessor<C>> list = new ArrayList<>();
      flattenChain(list, links);
      this.links = Collections.unmodifiableList(list);
   }

   private static <C> void flattenChain(final @NonNull List<SuggestionProcessor<C>> into, final @NonNull Collection<SuggestionProcessor<C>> links) {
      for (SuggestionProcessor<C> link : links) {
         if (link instanceof ChainedSuggestionProcessor) {
            flattenChain(into, ((ChainedSuggestionProcessor)link).links);
         } else {
            into.add(link);
         }
      }
   }

   @Override
   public @NonNull Stream<@NonNull Suggestion> process(
      final @NonNull CommandPreprocessingContext<C> context, final @NonNull Stream<@NonNull Suggestion> suggestions
   ) {
      Stream<Suggestion> currentLink = suggestions;

      for (SuggestionProcessor<C> link : this.links) {
         currentLink = link.process(context, currentLink);
      }

      return currentLink;
   }
}
