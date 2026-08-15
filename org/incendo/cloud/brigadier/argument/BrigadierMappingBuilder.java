package org.incendo.cloud.brigadier.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.parser.ArgumentParser;

public interface BrigadierMappingBuilder<K extends ArgumentParser<?, ?>, S> {
   @This @NonNull BrigadierMappingBuilder<K, S> toConstant(ArgumentType<?> constant);

   @This @NonNull BrigadierMappingBuilder<K, S> to(Function<K, ? extends ArgumentType<?>> mapper);

   @This @NonNull BrigadierMappingBuilder<K, S> nativeSuggestions();

   @This @NonNull BrigadierMappingBuilder<K, S> cloudSuggestions();

   default @This @NonNull BrigadierMappingBuilder<K, S> suggestedByConstant(final SuggestionProvider<S> provider) {
      Objects.requireNonNull(provider, "provider");
      return this.suggestedBy((argument, useCloud) -> provider);
   }

   @This @NonNull BrigadierMappingBuilder<K, S> suggestedBy(BrigadierMappingBuilder.SuggestionProviderSupplier<K, S> provider);

   @NonNull BrigadierMapping<?, K, S> build();

   @FunctionalInterface
   interface SuggestionProviderSupplier<K extends ArgumentParser<?, ?>, S> {
      @Nullable SuggestionProvider<? super S> provide(@NonNull K argument, SuggestionProvider<S> useCloud);
   }
}
