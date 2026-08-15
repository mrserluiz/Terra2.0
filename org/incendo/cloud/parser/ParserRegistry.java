package org.incendo.cloud.parser;

import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.STABLE)
public interface ParserRegistry<C> {
   <T> @This ParserRegistry<C> registerParserSupplier(
      @NonNull TypeToken<T> type, @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
   );

   @API(status = Status.STABLE)
   default <T> @This ParserRegistry<C> registerParser(final @NonNull ParserDescriptor<C, T> descriptor) {
      return this.registerParserSupplier(descriptor.valueType(), parameters -> descriptor.parser());
   }

   @This ParserRegistry<C> registerNamedParserSupplier(
      @NonNull String name, @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
   );

   default @This ParserRegistry<C> registerNamedParser(@NonNull String name, @NonNull ParserDescriptor<C, ?> descriptor) {
      return this.registerNamedParserSupplier(name, parameters -> descriptor.parser());
   }

   <A extends Annotation> @This ParserRegistry<C> registerAnnotationMapper(@NonNull Class<A> annotation, ParserRegistry.@NonNull AnnotationMapper<A> mapper);

   @NonNull ParserParameters parseAnnotations(@NonNull TypeToken<?> parsingType, @NonNull Collection<? extends @NonNull Annotation> annotations);

   <T> @NonNull Optional<ArgumentParser<C, T>> createParser(@NonNull TypeToken<T> type, @NonNull ParserParameters parserParameters);

   <T> @NonNull Optional<ArgumentParser<C, T>> createParser(@NonNull String name, @NonNull ParserParameters parserParameters);

   @API(status = Status.STABLE)
   void registerSuggestionProvider(@NonNull String name, @NonNull SuggestionProvider<C> suggestionProvider);

   @API(status = Status.STABLE)
   @NonNull Optional<SuggestionProvider<C>> getSuggestionProvider(@NonNull String name);

   @FunctionalInterface
   @API(status = Status.STABLE)
   interface AnnotationMapper<A extends Annotation> {
      @NonNull ParserParameters mapAnnotation(@NonNull A annotation, @NonNull TypeToken<?> parsedType);
   }
}
