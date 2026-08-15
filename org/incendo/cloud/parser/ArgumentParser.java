package org.incendo.cloud.parser;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.EitherParser;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.incendo.cloud.suggestion.SuggestionProviderHolder;
import org.incendo.cloud.type.Either;

@FunctionalInterface
@API(status = Status.STABLE)
public interface ArgumentParser<C, T> extends SuggestionProviderHolder<C> {
   @NonNull ArgumentParseResult<@NonNull T> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput);

   @API(status = Status.STABLE)
   default @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> parseFuture(
      @NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput
   ) {
      return CompletableFuture.completedFuture(this.parse(commandContext, commandInput));
   }

   @API(status = Status.STABLE)
   default <O> ArgumentParser.@NonNull FutureArgumentParser<C, O> flatMap(final MappedArgumentParser.Mapper<C, T, O> mapper) {
      return new MappedArgumentParserImpl<>(this, Objects.requireNonNull(mapper, "mapper"));
   }

   @API(status = Status.STABLE)
   default <O> ArgumentParser.@NonNull FutureArgumentParser<C, O> flatMapSuccess(
      final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<ArgumentParseResult<O>>> mapper
   ) {
      Objects.requireNonNull(mapper, "mapper");
      return this.flatMap((ctx, result) -> result.flatMapSuccessFuture(value -> mapper.apply(ctx, value)));
   }

   @API(status = Status.STABLE)
   default <O> ArgumentParser.@NonNull FutureArgumentParser<C, O> mapSuccess(final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<O>> mapper) {
      Objects.requireNonNull(mapper, "mapper");
      return this.flatMap((ctx, result) -> result.mapSuccessFuture(value -> mapper.apply(ctx, value)));
   }

   @Override
   default @NonNull SuggestionProvider<C> suggestionProvider() {
      return this instanceof SuggestionProvider ? (SuggestionProvider)this : SuggestionProvider.noSuggestions();
   }

   static <C, U, V> @NonNull ParserDescriptor<C, Either<U, V>> firstOf(
      final @NonNull ParserDescriptor<C, U> primary, final @NonNull ParserDescriptor<C, V> fallback
   ) {
      return EitherParser.eitherParser(primary, fallback);
   }

   @FunctionalInterface
   @API(status = Status.STABLE)
   interface FutureArgumentParser<C, T> extends ArgumentParser<C, T> {
      @Override
      default @NonNull ArgumentParseResult<@NonNull T> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
         throw new UnsupportedOperationException("parse should not be called on a FutureArgumentParser. Call parseFuture instead.");
      }

      @Override
      @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> parseFuture(@NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput);
   }
}
