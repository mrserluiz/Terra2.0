package org.incendo.cloud.parser.standard;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.incendo.cloud.type.Either;

@API(status = Status.STABLE)
public final class EitherParser<C, U, V> implements ArgumentParser.FutureArgumentParser<C, Either<U, V>>, SuggestionProvider<C> {
   private final ParserDescriptor<C, U> primary;
   private final ParserDescriptor<C, V> fallback;

   public static <C, U, V> ParserDescriptor<C, Either<U, V>> eitherParser(
      final @NonNull ParserDescriptor<C, U> primary, final @NonNull ParserDescriptor<C, V> fallback
   ) {
      return ParserDescriptor.of(
         new EitherParser<>(primary, fallback),
         (TypeToken<Either<U, V>>)TypeToken.get(TypeFactory.parameterizedClass(Either.class, primary.valueType().getType(), fallback.valueType().getType()))
      );
   }

   public EitherParser(final @NonNull ParserDescriptor<C, U> primary, final @NonNull ParserDescriptor<C, V> fallback) {
      this.primary = Objects.requireNonNull(primary, "primary");
      this.fallback = Objects.requireNonNull(fallback, "fallback");
   }

   public @NonNull ParserDescriptor<C, U> primary() {
      return this.primary;
   }

   public @NonNull ParserDescriptor<C, V> fallback() {
      return this.fallback;
   }

   @Override
   public @NonNull CompletableFuture<@NonNull ArgumentParseResult<Either<U, V>>> parseFuture(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      String input = commandInput.peekString();
      int originalCursor = commandInput.cursor();
      return this.primary
         .parser()
         .parseFuture(commandContext, commandInput)
         .thenCompose(
            primaryResult -> {
               if (primaryResult.parsedValue().isPresent()) {
                  return ArgumentParseResult.successFuture(Either.ofPrimary(primaryResult.parsedValue().get()));
               }

               commandInput.cursor(originalCursor);
               return this.fallback
                  .parser()
                  .parseFuture(commandContext, commandInput)
                  .thenApply(
                     fallbackResult -> fallbackResult.parsedValue().isPresent()
                        ? ArgumentParseResult.success(Either.ofFallback(fallbackResult.parsedValue().get()))
                        : ArgumentParseResult.failure(
                           new EitherParser.EitherParseException(
                              primaryResult.failure().get(),
                              fallbackResult.failure().get(),
                              this.primary.valueType(),
                              this.fallback.valueType(),
                              commandContext,
                              input
                           )
                        )
                  );
            }
         );
   }

   @Override
   public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(
      final @NonNull CommandContext<C> context, final @NonNull CommandInput input
   ) {
      if (!(this.primary.parser() instanceof SuggestionProvider)) {
         return !(this.fallback.parser() instanceof SuggestionProvider)
            ? CompletableFuture.completedFuture(Collections.emptyList())
            : ((SuggestionProvider)this.fallback.parser()).suggestionsFuture(context, input);
      }

      if (!(this.fallback.parser() instanceof SuggestionProvider)) {
         return ((SuggestionProvider)this.primary.parser()).suggestionsFuture(context, input);
      }

      CompletableFuture<Iterable<Suggestion>>[] suggestionFutures = new CompletableFuture[]{
         ((SuggestionProvider)this.primary.parser()).suggestionsFuture(context, input.copy()),
         ((SuggestionProvider)this.fallback.parser()).suggestionsFuture(context, input)
      };
      return CompletableFuture.allOf(suggestionFutures)
         .thenApply(
            ignored -> Stream.concat(
                  StreamSupport.stream(suggestionFutures[0].getNow(Collections.emptyList()).spliterator(), false),
                  StreamSupport.stream(suggestionFutures[1].getNow(Collections.emptyList()).spliterator(), false)
               )
               .collect(Collectors.toList())
         );
   }

   public static final class EitherParseException extends ParserException {
      private final Throwable primaryFailure;
      private final Throwable fallbackFailure;
      private final TypeToken<?> primaryType;
      private final TypeToken<?> fallbackType;

      private EitherParseException(
         final @NonNull Throwable primaryFailure,
         final @NonNull Throwable fallbackFailure,
         final @NonNull TypeToken<?> primaryType,
         final @NonNull TypeToken<?> fallbackType,
         final @NonNull CommandContext<?> context,
         final @NonNull String input
      ) {
         super(
            fallbackFailure,
            EitherParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_EITHER,
            CaptionVariable.of("input", input),
            CaptionVariable.of("primary", GenericTypeReflector.erase(primaryType.getType()).getSimpleName()),
            CaptionVariable.of("fallback", GenericTypeReflector.erase(fallbackType.getType()).getSimpleName())
         );
         this.primaryFailure = primaryFailure;
         this.fallbackFailure = fallbackFailure;
         this.primaryType = primaryType;
         this.fallbackType = fallbackType;
      }

      public @NonNull Throwable primaryFailure() {
         return this.primaryFailure;
      }

      public @NonNull Throwable fallbackFailure() {
         return this.fallbackFailure;
      }

      public @NonNull TypeToken<?> primaryType() {
         return this.primaryType;
      }

      public @NonNull TypeToken<?> fallbackType() {
         return this.fallbackType;
      }
   }
}
