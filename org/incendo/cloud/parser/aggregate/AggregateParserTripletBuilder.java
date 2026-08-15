package org.incendo.cloud.parser.aggregate;

import io.leangen.geantyref.TypeToken;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.type.tuple.Triplet;

public final class AggregateParserTripletBuilder<C, U, V, Z, O> {
   private final AggregateParserTripletBuilder.Mapper<C, U, V, Z, O> mapper;
   private final TypeToken<O> outType;
   private final TypedCommandComponent<C, U> first;
   private final TypedCommandComponent<C, V> second;
   private final TypedCommandComponent<C, Z> third;

   public static <C, U, V, Z> AggregateParserTripletBuilder.Mapper<C, U, V, Z, Triplet<U, V, Z>> defaultMapper() {
      return (ctx, u, v, z) -> ArgumentParseResult.successFuture(Triplet.of(u, v, z));
   }

   public AggregateParserTripletBuilder(
      final TypedCommandComponent<C, U> first,
      final TypedCommandComponent<C, V> second,
      final TypedCommandComponent<C, Z> third,
      final AggregateParserTripletBuilder.Mapper<C, U, V, Z, O> mapper,
      final TypeToken<O> outType
   ) {
      this.mapper = mapper;
      this.outType = outType;
      this.first = first;
      this.second = second;
      this.third = third;
   }

   public <O1> AggregateParserTripletBuilder<C, U, V, Z, O1> withMapper(
      final @NonNull TypeToken<O1> outType, final AggregateParserTripletBuilder.@NonNull Mapper<C, U, V, Z, O1> mapper
   ) {
      return new AggregateParserTripletBuilder<>(this.first, this.second, this.third, mapper, outType);
   }

   public <O1> AggregateParserTripletBuilder<C, U, V, Z, O1> withDirectMapper(
      final @NonNull TypeToken<O1> outType, final AggregateParserTripletBuilder.Mapper.@NonNull DirectSuccessMapper<C, U, V, Z, O1> mapper
   ) {
      return this.withMapper(outType, mapper);
   }

   public AggregateParser<C, O> build() {
      return new AggregateParserBuilder<>(Arrays.asList(this.first, this.second, this.third)).withMapper(this.outType, (commandContext, aggregateContext) -> {
         U firstResult = aggregateContext.get(this.first.name());
         V secondResult = aggregateContext.get(this.second.name());
         Z thirdResult = aggregateContext.get(this.third.name());
         return this.mapper.map(commandContext, firstResult, secondResult, thirdResult);
      }).build();
   }

   public static <C, U, V, Z, O> AggregateParserTripletBuilder.@NonNull Mapper<C, U, V, Z, O> directMapper(
      final AggregateParserTripletBuilder.Mapper.@NonNull DirectSuccessMapper<C, U, V, Z, O> mapper
   ) {
      return Objects.requireNonNull(mapper, "mapper");
   }

   public interface Mapper<C, U, V, Z, O> {
      @NonNull CompletableFuture<ArgumentParseResult<O>> map(
         @NonNull CommandContext<C> commandContext, @NonNull U firstResult, @NonNull V secondResult, @NonNull Z thirdResult
      );

      interface DirectSuccessMapper<C, U, V, Z, O> extends AggregateParserTripletBuilder.Mapper<C, U, V, Z, O> {
         @NonNull O mapSuccess(@NonNull CommandContext<C> commandContext, @NonNull U firstResult, @NonNull V secondResult, @NonNull Z thirdResult);

         @Override
         default @NonNull CompletableFuture<ArgumentParseResult<O>> map(
            @NonNull CommandContext<C> commandContext, @NonNull U firstResult, @NonNull V secondResult, @NonNull Z thirdResult
         ) {
            return ArgumentParseResult.successFuture(this.mapSuccess(commandContext, firstResult, secondResult, thirdResult));
         }
      }
   }
}
