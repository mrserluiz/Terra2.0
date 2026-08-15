package org.incendo.cloud.parser.aggregate;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.type.tuple.Triplet;

@API(status = Status.STABLE)
public interface AggregateParser<C, O> extends ArgumentParser.FutureArgumentParser<C, O>, ParserDescriptor<C, O> {
   static <C> @NonNull AggregateParserBuilder<C> builder() {
      return new AggregateParserBuilder<>();
   }

   static <C, U, V> @NonNull AggregateParserPairBuilder<C, U, V, Pair<U, V>> pairBuilder(
      final @NonNull String firstName,
      final @NonNull ParserDescriptor<C, U> firstParser,
      final @NonNull String secondName,
      final @NonNull ParserDescriptor<C, V> secondParser
   ) {
      return new AggregateParserPairBuilder<>(
         CommandComponent.builder(firstName, firstParser).build(),
         CommandComponent.builder(secondName, secondParser).build(),
         AggregateParserPairBuilder.defaultMapper(),
         (TypeToken<Pair<U, V>>)TypeToken.get(
            TypeFactory.parameterizedClass(
               Pair.class, GenericTypeReflector.box(firstParser.valueType().getType()), GenericTypeReflector.box(secondParser.valueType().getType())
            )
         )
      );
   }

   static <C, U, V, Z> @NonNull AggregateParserTripletBuilder<C, U, V, Z, Triplet<U, V, Z>> tripletBuilder(
      final @NonNull String firstName,
      final @NonNull ParserDescriptor<C, U> firstParser,
      final @NonNull String secondName,
      final @NonNull ParserDescriptor<C, V> secondParser,
      final @NonNull String thirdName,
      final @NonNull ParserDescriptor<C, Z> thirdParser
   ) {
      return new AggregateParserTripletBuilder<>(
         CommandComponent.builder(firstName, firstParser).build(),
         CommandComponent.builder(secondName, secondParser).build(),
         CommandComponent.builder(thirdName, thirdParser).build(),
         AggregateParserTripletBuilder.defaultMapper(),
         (TypeToken<Triplet<U, V, Z>>)TypeToken.get(
            TypeFactory.parameterizedClass(
               Triplet.class,
               GenericTypeReflector.box(firstParser.valueType().getType()),
               GenericTypeReflector.box(secondParser.valueType().getType()),
               GenericTypeReflector.box(thirdParser.valueType().getType())
            )
         )
      );
   }

   @NonNull List<@NonNull CommandComponent<C>> components();

   @NonNull AggregateResultMapper<C, O> mapper();

   @Override
   default @NonNull CompletableFuture<@NonNull ArgumentParseResult<O>> parseFuture(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      AggregateParsingContext<C> aggregateParsingContext = AggregateParsingContext.argumentContext(this);
      CompletableFuture<ArgumentParseResult<Object>> future = CompletableFuture.completedFuture(null);

      for (CommandComponent<C> component : this.components()) {
         future = future.thenCompose(
            result -> {
               if (result != null && result.failure().isPresent()) {
                  return ArgumentParseResult.failureFuture(result.failure().get());
               }

               commandInput.skipWhitespace(1);
               return commandInput.isEmpty()
                  ? ArgumentParseResult.failureFuture(new AggregateParser.AggregateParseException(commandContext, component))
                  : component.parser().parseFuture(commandContext, commandInput).thenApply(value -> {
                     if (value.parsedValue().isPresent()) {
                        CloudKey key = CloudKey.of(component.name(), component.valueType());
                        aggregateParsingContext.store(key, value.parsedValue().get());
                     } else if (value.failure().isPresent()) {
                        return ArgumentParseResult.failure(new AggregateParser.AggregateParseException(commandContext, "", component, value.failure().get()));
                     }

                     return (ArgumentParseResult<Object>)value;
                  });
            }
         );
      }

      return future.thenCompose(
         result -> (CompletionStage<ArgumentParseResult<O>>)(result != null && result.failure().isPresent()
            ? result.asFuture()
            : this.mapper().map(commandContext, aggregateParsingContext))
      );
   }

   @Override
   default @NonNull SuggestionProvider<C> suggestionProvider() {
      return new AggregateSuggestionProvider<>(this);
   }

   @Override
   default @NonNull ArgumentParser<C, O> parser() {
      return this;
   }

   @API(status = Status.STABLE)
   final class AggregateParseException extends ParserException {
      private AggregateParseException(
         final @NonNull CommandContext<?> context, final @NonNull String input, final @NonNull CommandComponent<?> component, final @NonNull Throwable cause
      ) {
         super(
            cause,
            AggregateParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE,
            CaptionVariable.of("input", input),
            CaptionVariable.of("component", component.name()),
            CaptionVariable.of("failure", cause.getMessage())
         );
      }

      private AggregateParseException(final @NonNull CommandContext<?> context, final @NonNull CommandComponent<?> component) {
         super(
            AggregateParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT,
            CaptionVariable.of("component", component.name())
         );
      }
   }
}
