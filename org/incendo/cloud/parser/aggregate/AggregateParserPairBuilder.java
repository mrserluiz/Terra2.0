package org.incendo.cloud.parser.aggregate;

import io.leangen.geantyref.TypeToken;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.type.tuple.Pair;

public final class AggregateParserPairBuilder<C, U, V, O> {
   private final AggregateParserPairBuilder.Mapper<C, U, V, O> mapper;
   private final TypeToken<O> outType;
   private final TypedCommandComponent<C, U> first;
   private final TypedCommandComponent<C, V> second;

   public static <C, U, V> AggregateParserPairBuilder.Mapper<C, U, V, Pair<U, V>> defaultMapper() {
      return (ctx, u, v) -> ArgumentParseResult.successFuture(Pair.of(u, v));
   }

   public AggregateParserPairBuilder(
      final TypedCommandComponent<C, U> first,
      final TypedCommandComponent<C, V> second,
      final AggregateParserPairBuilder.Mapper<C, U, V, O> mapper,
      final TypeToken<O> outType
   ) {
      this.mapper = mapper;
      this.outType = outType;
      this.first = first;
      this.second = second;
   }

   public <O1> AggregateParserPairBuilder<C, U, V, O1> withMapper(
      final @NonNull TypeToken<O1> outType, final AggregateParserPairBuilder.@NonNull Mapper<C, U, V, O1> mapper
   ) {
      return new AggregateParserPairBuilder<>(this.first, this.second, mapper, outType);
   }

   public <O1> AggregateParserPairBuilder<C, U, V, O1> withDirectMapper(
      final @NonNull TypeToken<O1> outType, final AggregateParserPairBuilder.Mapper.@NonNull DirectSuccessMapper<C, U, V, O1> mapper
   ) {
      return this.withMapper(outType, mapper);
   }

   public AggregateParser<C, O> build() {
      return new AggregateParserBuilder<>(Arrays.asList(this.first, this.second)).withMapper(this.outType, (commandContext, aggregateContext) -> {
         U firstResult = aggregateContext.get(this.first.name());
         V secondResult = aggregateContext.get(this.second.name());
         return this.mapper.map(commandContext, firstResult, secondResult);
      }).build();
   }

   public static <C, U, V, O> AggregateParserPairBuilder.@NonNull Mapper<C, U, V, O> directMapper(
      final AggregateParserPairBuilder.Mapper.@NonNull DirectSuccessMapper<C, U, V, O> mapper
   ) {
      return Objects.requireNonNull(mapper, "mapper");
   }

   public interface Mapper<C, U, V, O> {
      @NonNull CompletableFuture<ArgumentParseResult<O>> map(@NonNull CommandContext<C> commandContext, @NonNull U firstResult, @NonNull V secondResult);

      interface DirectSuccessMapper<C, U, V, O> extends AggregateParserPairBuilder.Mapper<C, U, V, O> {
         @NonNull O mapSuccess(@NonNull CommandContext<C> commandContext, @NonNull U firstResult, @NonNull V secondResult);

         @Override
         default @NonNull CompletableFuture<ArgumentParseResult<O>> map(
            @NonNull CommandContext<C> commandContext, @NonNull U firstResult, @NonNull V secondResult
         ) {
            return ArgumentParseResult.successFuture(this.mapSuccess(commandContext, firstResult, secondResult));
         }
      }
   }
}
