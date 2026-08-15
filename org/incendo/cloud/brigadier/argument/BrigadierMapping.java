package org.incendo.cloud.brigadier.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Objects;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.parser.ArgumentParser;

@API(status = Status.INTERNAL, since = "2.0.0")
public final class BrigadierMapping<C, K extends ArgumentParser<C, ?>, S> {
   private static final SuggestionProvider<?> DELEGATE_TO_CLOUD = (c, b) -> b.buildFuture();
   private final boolean cloudSuggestions;
   private final BrigadierMappingBuilder.@Nullable SuggestionProviderSupplier<K, S> suggestionsOverride;
   private final @Nullable Function<K, ? extends ArgumentType<?>> mapper;

   public static <T> SuggestionProvider<T> delegateSuggestions() {
      return (SuggestionProvider<T>)DELEGATE_TO_CLOUD;
   }

   public static <C, K extends ArgumentParser<C, ?>, S> @NonNull BrigadierMappingBuilder<K, S> builder() {
      return new BrigadierMapping.BuilderImpl<>();
   }

   BrigadierMapping(
      final boolean cloudSuggestions,
      final BrigadierMappingBuilder.@Nullable SuggestionProviderSupplier<K, S> suggestionsOverride,
      final @Nullable Function<K, ? extends ArgumentType<?>> mapper
   ) {
      this.cloudSuggestions = cloudSuggestions;
      this.suggestionsOverride = suggestionsOverride;
      this.mapper = mapper;
   }

   public @Nullable Function<K, ? extends ArgumentType<?>> mapper() {
      return this.mapper;
   }

   public @NonNull BrigadierMapping<C, K, S> withNativeSuggestions(final boolean nativeSuggestions) {
      if (nativeSuggestions && this.cloudSuggestions) {
         return new BrigadierMapping<>(false, this.suggestionsOverride, this.mapper);
      } else {
         return !nativeSuggestions && !this.cloudSuggestions ? new BrigadierMapping<>(true, this.suggestionsOverride, this.mapper) : this;
      }
   }

   public @Nullable SuggestionProvider<S> makeSuggestionProvider(final K commandArgument) {
      if (this.cloudSuggestions) {
         return delegateSuggestions();
      } else {
         return (SuggestionProvider<S>)(this.suggestionsOverride == null ? null : this.suggestionsOverride.provide(commandArgument, delegateSuggestions()));
      }
   }

   private static final class BuilderImpl<C, K extends ArgumentParser<C, ?>, S> implements BrigadierMappingBuilder<K, S> {
      private Function<K, ? extends ArgumentType<?>> mapper;
      private boolean cloudSuggestions = false;
      private BrigadierMappingBuilder.SuggestionProviderSupplier<K, S> suggestionsOverride;

      private BuilderImpl() {
      }

      @Override
      public @This @NonNull BrigadierMappingBuilder<K, S> toConstant(final ArgumentType<?> constant) {
         return this.to(parser -> constant);
      }

      @Override
      public @This @NonNull BrigadierMappingBuilder<K, S> to(final Function<K, ? extends ArgumentType<?>> mapper) {
         this.mapper = mapper;
         return this;
      }

      @Override
      public @This @NonNull BrigadierMappingBuilder<K, S> nativeSuggestions() {
         this.cloudSuggestions = false;
         this.suggestionsOverride = null;
         return this;
      }

      @Override
      public @This @NonNull BrigadierMappingBuilder<K, S> cloudSuggestions() {
         this.cloudSuggestions = true;
         this.suggestionsOverride = null;
         return this;
      }

      @Override
      public @This @NonNull BrigadierMappingBuilder<K, S> suggestedByConstant(final SuggestionProvider<S> provider) {
         BrigadierMappingBuilder.super.suggestedByConstant(provider);
         this.cloudSuggestions = false;
         return this;
      }

      @Override
      public @This @NonNull BrigadierMappingBuilder<K, S> suggestedBy(final BrigadierMappingBuilder.SuggestionProviderSupplier<K, S> provider) {
         this.suggestionsOverride = Objects.requireNonNull(provider, "provider");
         this.cloudSuggestions = false;
         return this;
      }

      @Override
      public @NonNull BrigadierMapping<C, K, S> build() {
         return new BrigadierMapping<>(this.cloudSuggestions, this.suggestionsOverride, this.mapper);
      }
   }
}
