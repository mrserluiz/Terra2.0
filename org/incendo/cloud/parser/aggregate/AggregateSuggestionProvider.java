package org.incendo.cloud.parser.aggregate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.INTERNAL)
final class AggregateSuggestionProvider<C> implements SuggestionProvider<C> {
   private final AggregateParser<C, ?> parser;

   AggregateSuggestionProvider(final @NonNull AggregateParser<C, ?> parser) {
      this.parser = parser;
   }

   @Override
   public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
      final @NonNull CommandContext<C> context, final @NonNull CommandInput input
   ) {
      CommandInput originalInput = input.copy();
      return new AggregateSuggestionProvider.ParsingInstance(context, input)
         .parseComponent()
         .thenCompose(component -> component.suggestionProvider().suggestionsFuture(context, input.skipWhitespace(1, false).copy()))
         .thenApply(suggestions -> {
            String prefix = originalInput.difference(input, true);
            List<Suggestion> prefixedSuggestions = new ArrayList<>();

            for (Suggestion suggestion : suggestions) {
               prefixedSuggestions.add(suggestion.withSuggestion(String.format("%s%s", prefix, suggestion.suggestion())));
            }

            return prefixedSuggestions;
         });
   }

   private final class ParsingInstance {
      private final Iterator<CommandComponent<C>> components = AggregateSuggestionProvider.this.parser.components().iterator();
      private final CommandContext<C> context;
      private final CommandInput input;
      private CommandComponent<C> component;
      private int previousCursor;

      private ParsingInstance(final @NonNull CommandContext<C> context, final @NonNull CommandInput input) {
         this.context = context;
         this.input = input;
      }

      private @NonNull CompletableFuture<CommandComponent<C>> parseComponent() {
         if (!this.components.hasNext()) {
            return CompletableFuture.completedFuture(this.component);
         }

         this.component = this.components.next();
         this.previousCursor = this.input.cursor();
         return this.component.parser().parseFuture(this.context, this.input.skipWhitespace(1)).thenCompose(this::handleResult);
      }

      private @NonNull CompletableFuture<CommandComponent<C>> handleResult(final @NonNull ArgumentParseResult<?> result) {
         boolean consumedAll = this.input.isEmpty();
         if (result.failure().isPresent() || !this.components.hasNext() || this.input.isEmpty()) {
            this.input.cursor(this.previousCursor);
         }

         if (result.failure().isPresent()) {
            return CompletableFuture.completedFuture(this.component);
         }

         result.parsedValue().ifPresent(value -> this.context.store(this.component.name(), value));
         return consumedAll ? CompletableFuture.completedFuture(this.component) : this.parseComponent();
      }
   }
}
