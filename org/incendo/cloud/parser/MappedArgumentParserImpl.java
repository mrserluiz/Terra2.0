package org.incendo.cloud.parser;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.INTERNAL)
public final class MappedArgumentParserImpl<C, I, O> implements MappedArgumentParser<C, I, O>, ArgumentParser.FutureArgumentParser<C, O> {
   private final ArgumentParser<C, I> base;
   private final MappedArgumentParser.Mapper<C, I, O> mapper;

   MappedArgumentParserImpl(final ArgumentParser<C, I> base, final MappedArgumentParser.Mapper<C, I, O> mapper) {
      this.base = base;
      this.mapper = mapper;
   }

   @Override
   public @NonNull ArgumentParser<C, I> baseParser() {
      return this.base;
   }

   @Override
   public @NonNull CompletableFuture<@NonNull ArgumentParseResult<O>> parseFuture(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      return this.base.parseFuture(commandContext, commandInput).thenCompose(result -> this.mapper.map(commandContext, (ArgumentParseResult<I>)result));
   }

   @Override
   public @NonNull SuggestionProvider<C> suggestionProvider() {
      return this.base.suggestionProvider();
   }

   @Override
   public <O1> ArgumentParser.@NonNull FutureArgumentParser<C, O1> flatMap(final MappedArgumentParser.Mapper<C, O, O1> mapper) {
      Objects.requireNonNull(mapper, "mapper");
      return new MappedArgumentParserImpl<>(
         this.base,
         (ctx, orig) -> this.mapper
            .map(ctx, orig)
            .thenCompose(mapped -> (CompletionStage<ArgumentParseResult<O>>)mapper.map(ctx, (ArgumentParseResult<O>)mapped))
      );
   }

   @Override
   public int hashCode() {
      return 31 + this.base.hashCode() + 7 * this.mapper.hashCode();
   }

   @Override
   public boolean equals(final @Nullable Object other) {
      if (!(other instanceof MappedArgumentParserImpl)) {
         return false;
      }

      MappedArgumentParserImpl<?, ?, ?> that = (MappedArgumentParserImpl<?, ?, ?>)other;
      return this.base.equals(that.base) && this.mapper.equals(that.mapper);
   }

   @Override
   public String toString() {
      return "MappedArgumentParserImpl{base=" + this.base + ',' + "mapper=" + this.mapper + '}';
   }
}
