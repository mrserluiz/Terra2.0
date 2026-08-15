package org.incendo.cloud.parser;

import io.leangen.geantyref.TypeToken;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.incendo.cloud.context.CommandContext;

@API(status = Status.STABLE)
@Immutable
public interface ParserDescriptor<C, T> {
   @NonNull ArgumentParser<C, T> parser();

   @NonNull TypeToken<T> valueType();

   default <O> @NonNull ParserDescriptor<C, O> flatMap(final @NonNull TypeToken<O> mappedType, final MappedArgumentParser.@NonNull Mapper<C, T, O> mapper) {
      return parserDescriptor(this.parser().flatMap(mapper), mappedType);
   }

   default <O> @NonNull ParserDescriptor<C, O> flatMap(final @NonNull Class<O> mappedType, final MappedArgumentParser.@NonNull Mapper<C, T, O> mapper) {
      return parserDescriptor(this.parser().flatMap(mapper), mappedType);
   }

   default <O> @NonNull ParserDescriptor<C, O> flatMapSuccess(
      final @NonNull TypeToken<O> mappedType, final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<ArgumentParseResult<O>>> mapper
   ) {
      return parserDescriptor(this.parser().flatMapSuccess(mapper), mappedType);
   }

   default <O> @NonNull ParserDescriptor<C, O> flatMapSuccess(
      final @NonNull Class<O> mappedType, final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<ArgumentParseResult<O>>> mapper
   ) {
      return parserDescriptor(this.parser().flatMapSuccess(mapper), mappedType);
   }

   default <O> @NonNull ParserDescriptor<C, O> mapSuccess(
      final @NonNull TypeToken<O> mappedType, final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<O>> mapper
   ) {
      return parserDescriptor(this.parser().mapSuccess(mapper), mappedType);
   }

   default <O> @NonNull ParserDescriptor<C, O> mapSuccess(
      final @NonNull Class<O> mappedType, final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<O>> mapper
   ) {
      return parserDescriptor(this.parser().mapSuccess(mapper), mappedType);
   }

   static <C, T> @NonNull ParserDescriptor<C, T> of(final @NonNull ArgumentParser<C, T> parser, final @NonNull TypeToken<T> valueType) {
      return ParserDescriptorImpl.of(parser, valueType);
   }

   static <C, T> @NonNull ParserDescriptor<C, T> of(final @NonNull ArgumentParser<C, T> parser, final @NonNull Class<T> valueType) {
      return ParserDescriptorImpl.of(parser, TypeToken.get(valueType));
   }

   static <C, T> @NonNull ParserDescriptor<C, T> parserDescriptor(final @NonNull ArgumentParser<C, T> parser, final @NonNull TypeToken<T> valueType) {
      return of(parser, valueType);
   }

   static <C, T> @NonNull ParserDescriptor<C, T> parserDescriptor(final @NonNull ArgumentParser<C, T> parser, final @NonNull Class<T> valueType) {
      return of(parser, TypeToken.get(valueType));
   }
}
