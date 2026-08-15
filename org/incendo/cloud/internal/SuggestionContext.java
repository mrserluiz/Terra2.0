package org.incendo.cloud.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.SuggestionProcessor;
import org.incendo.cloud.suggestion.Suggestions;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class SuggestionContext<C, S extends Suggestion> {
   private final List<S> suggestions = new ArrayList<>();
   private final CommandPreprocessingContext<C> preprocessingContext;
   private final SuggestionMapper<S> mapper;
   private final SuggestionProcessor<C> processor;
   private final CommandContext<C> commandContext;

   public SuggestionContext(
      final @NonNull SuggestionProcessor<C> processor,
      final @NonNull CommandContext<C> commandContext,
      final @NonNull CommandInput commandInput,
      final @NonNull SuggestionMapper<S> mapper
   ) {
      this.processor = processor;
      this.commandContext = commandContext;
      this.preprocessingContext = CommandPreprocessingContext.of(this.commandContext, commandInput);
      this.mapper = mapper;
   }

   public @NonNull Suggestions<C, S> makeSuggestions() {
      Stream<S> stream = this.suggestions.stream();
      Stream<Suggestion> processedStream = this.processor.process(this.preprocessingContext, stream);
      List<S> list;
      if (stream == processedStream) {
         list = Collections.unmodifiableList(this.suggestions);
      } else {
         list = Collections.unmodifiableList(
            processedStream.peek(obj -> Objects.requireNonNull(obj, "suggestion")).map(this.mapper::map).collect(Collectors.toList())
         );
      }

      return Suggestions.create(this.commandContext, list, this.preprocessingContext.commandInput());
   }

   public @NonNull CommandContext<C> commandContext() {
      return this.commandContext;
   }

   public void addSuggestions(final @NonNull Iterable<? extends @NonNull Suggestion> suggestions) {
      suggestions.forEach(this::addSuggestion);
   }

   public void addSuggestion(final @NonNull Suggestion suggestion) {
      Objects.requireNonNull(suggestion, "suggestion");
      this.suggestions.add(this.mapper.map(suggestion));
   }
}
