package org.incendo.cloud.suggestion;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.internal.CommandInputTokenizer;

@API(status = Status.STABLE)
public final class FilteringSuggestionProcessor<C> implements SuggestionProcessor<C> {
   private final FilteringSuggestionProcessor.@NonNull Filter<C> filter;

   @API(status = Status.STABLE)
   public FilteringSuggestionProcessor() {
      this(FilteringSuggestionProcessor.Filter.partialTokenMatches(true));
   }

   @API(status = Status.STABLE)
   public FilteringSuggestionProcessor(final FilteringSuggestionProcessor.@NonNull Filter<C> filter) {
      this.filter = filter;
   }

   @Override
   public @NonNull Stream<@NonNull Suggestion> process(
      final @NonNull CommandPreprocessingContext<C> context, final @NonNull Stream<@NonNull Suggestion> suggestions
   ) {
      String input;
      if (context.commandInput().isEmpty(true)) {
         input = "";
      } else {
         input = context.commandInput().skipWhitespace().remainingInput();
      }

      return suggestions.<Suggestion>map(suggestion -> {
         String filtered = this.filter.filter(context, suggestion.suggestion(), input);
         return filtered == null ? null : suggestion.withSuggestion(filtered);
      }).filter(Objects::nonNull);
   }

   @API(status = Status.STABLE)
   @FunctionalInterface
   public interface Filter<C> {
      @API(status = Status.STABLE)
      @Nullable String filter(@NonNull CommandPreprocessingContext<C> context, @NonNull String suggestion, @NonNull String input);

      @API(status = Status.STABLE)
      default FilteringSuggestionProcessor.@NonNull Filter<C> and(final FilteringSuggestionProcessor.@NonNull Filter<C> and) {
         return (ctx, suggestion, input) -> {
            String filtered = this.filter(ctx, suggestion, input);
            return filtered == null ? null : and.filter(ctx, filtered, input);
         };
      }

      @API(status = Status.STABLE)
      static <C> FilteringSuggestionProcessor.Filter.@NonNull Simple<C> startsWith(final boolean ignoreCase) {
         BiPredicate<String, String> test = ignoreCase
            ? (suggestion, input) -> suggestion.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))
            : String::startsWith;
         return FilteringSuggestionProcessor.Filter.Simple.contextFree(test);
      }

      @API(status = Status.STABLE)
      static <C> FilteringSuggestionProcessor.Filter.@NonNull Simple<C> contains(final boolean ignoreCase) {
         BiPredicate<String, String> test = ignoreCase
            ? (suggestion, input) -> suggestion.toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT))
            : String::contains;
         return FilteringSuggestionProcessor.Filter.Simple.contextFree(test);
      }

      @API(status = Status.STABLE)
      static <C> FilteringSuggestionProcessor.Filter.@NonNull Simple<C> partialTokenMatches(final boolean ignoreCase) {
         return FilteringSuggestionProcessor.Filter.Simple.contextFree((suggestion, input) -> {
            List<String> suggestionTokens = new CommandInputTokenizer(suggestion).tokenize();
            List<String> inputTokens = new CommandInputTokenizer(input).tokenize();
            boolean passed = true;

            for (String inputToken : inputTokens) {
               if (ignoreCase) {
                  inputToken = inputToken.toLowerCase(Locale.ROOT);
               }

               boolean foundMatch = false;
               Iterator<String> iterator = suggestionTokens.iterator();

               while (iterator.hasNext()) {
                  String suggestionToken = iterator.next();
                  String suggestionTokenLower = ignoreCase ? suggestionToken.toLowerCase(Locale.ROOT) : suggestionToken;
                  if (suggestionTokenLower.contains(inputToken)) {
                     iterator.remove();
                     foundMatch = true;
                     break;
                  }
               }

               if (!foundMatch) {
                  passed = false;
                  break;
               }
            }

            return passed;
         });
      }

      @API(status = Status.STABLE)
      static <C> FilteringSuggestionProcessor.@NonNull Filter<C> contextFree(final @NonNull BiFunction<String, String, @Nullable String> function) {
         return (ctx, suggestion, input) -> function.apply(suggestion, input);
      }

      @API(status = Status.STABLE)
      static <C> FilteringSuggestionProcessor.Filter.@NonNull Simple<C> simple(final FilteringSuggestionProcessor.Filter.Simple<C> filter) {
         return filter;
      }

      @API(status = Status.STABLE)
      @FunctionalInterface
      interface Simple<C> extends FilteringSuggestionProcessor.Filter<C> {
         @API(status = Status.STABLE)
         boolean test(@NonNull CommandPreprocessingContext<C> context, @NonNull String suggestion, @NonNull String input);

         @Override
         default @Nullable String filter(@NonNull CommandPreprocessingContext<C> context, @NonNull String suggestion, @NonNull String input) {
            return this.test(context, suggestion, input) ? suggestion : null;
         }

         @API(status = Status.STABLE)
         static <C> FilteringSuggestionProcessor.Filter.@NonNull Simple<C> contextFree(final @NonNull BiPredicate<String, String> test) {
            return (ctx, suggestion, input) -> test.test(suggestion, input);
         }
      }
   }
}
