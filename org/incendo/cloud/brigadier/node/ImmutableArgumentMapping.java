package org.incendo.cloud.brigadier.node;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Generated;
import org.incendo.cloud.brigadier.suggestion.SuggestionsType;

@API(status = Status.STABLE, consumers = "org.incendo.cloud.*")
@Generated(from = "ArgumentMapping", generator = "Immutables")
final class ImmutableArgumentMapping<S> implements ArgumentMapping<S> {
   private final @NonNull ArgumentType<?> argumentType;
   private final @NonNull SuggestionsType suggestionsType;
   private final @Nullable SuggestionProvider<S> suggestionProvider;

   private ImmutableArgumentMapping(
      @NonNull ArgumentType<?> argumentType, @NonNull SuggestionsType suggestionsType, @Nullable SuggestionProvider<S> suggestionProvider
   ) {
      this.argumentType = Objects.requireNonNull(argumentType, "argumentType");
      this.suggestionsType = Objects.requireNonNull(suggestionsType, "suggestionsType");
      this.suggestionProvider = suggestionProvider;
   }

   private ImmutableArgumentMapping(ImmutableArgumentMapping.Builder<S> builder) {
      this.argumentType = builder.argumentType;
      this.suggestionProvider = builder.suggestionProvider;
      this.suggestionsType = builder.suggestionsType != null
         ? builder.suggestionsType
         : Objects.requireNonNull(ArgumentMapping.super.suggestionsType(), "suggestionsType");
   }

   private ImmutableArgumentMapping(
      ImmutableArgumentMapping<S> original,
      @NonNull ArgumentType<?> argumentType,
      @NonNull SuggestionsType suggestionsType,
      @Nullable SuggestionProvider<S> suggestionProvider
   ) {
      this.argumentType = argumentType;
      this.suggestionsType = suggestionsType;
      this.suggestionProvider = suggestionProvider;
   }

   @Override
   public @NonNull ArgumentType<?> argumentType() {
      return this.argumentType;
   }

   @Override
   public @NonNull SuggestionsType suggestionsType() {
      return this.suggestionsType;
   }

   @Override
   public @Nullable SuggestionProvider<S> suggestionProvider() {
      return this.suggestionProvider;
   }

   public final ImmutableArgumentMapping<S> withArgumentType(ArgumentType<?> value) {
      if (this.argumentType == value) {
         return this;
      }

      ArgumentType<?> newValue = Objects.requireNonNull(value, "argumentType");
      return new ImmutableArgumentMapping<>(this, newValue, this.suggestionsType, this.suggestionProvider);
   }

   public final ImmutableArgumentMapping<S> withSuggestionsType(SuggestionsType value) {
      SuggestionsType newValue = Objects.requireNonNull(value, "suggestionsType");
      return this.suggestionsType == newValue ? this : new ImmutableArgumentMapping<>(this, this.argumentType, newValue, this.suggestionProvider);
   }

   public final ImmutableArgumentMapping<S> withSuggestionProvider(@Nullable SuggestionProvider<S> value) {
      return this.suggestionProvider == value ? this : new ImmutableArgumentMapping<>(this, this.argumentType, this.suggestionsType, value);
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ImmutableArgumentMapping && this.equalsByValue((ImmutableArgumentMapping<?>)another);
   }

   private boolean equalsByValue(ImmutableArgumentMapping<?> another) {
      return this.argumentType.equals(another.argumentType)
         && this.suggestionsType.equals(another.suggestionsType)
         && Objects.equals(this.suggestionProvider, another.suggestionProvider);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.argumentType.hashCode();
      h += (h << 5) + this.suggestionsType.hashCode();
      return h + (h << 5) + Objects.hashCode(this.suggestionProvider);
   }

   @Override
   public String toString() {
      return "ArgumentMapping{argumentType="
         + this.argumentType
         + ", suggestionsType="
         + this.suggestionsType
         + ", suggestionProvider="
         + this.suggestionProvider
         + "}";
   }

   public static <S> ImmutableArgumentMapping<S> of(
      @NonNull ArgumentType<?> argumentType, @NonNull SuggestionsType suggestionsType, @Nullable SuggestionProvider<S> suggestionProvider
   ) {
      return new ImmutableArgumentMapping<>(argumentType, suggestionsType, suggestionProvider);
   }

   public static <S> ImmutableArgumentMapping<S> copyOf(ArgumentMapping<S> instance) {
      return instance instanceof ImmutableArgumentMapping ? (ImmutableArgumentMapping)instance : builder().from(instance).build();
   }

   public static <S> ImmutableArgumentMapping.Builder<S> builder() {
      return new ImmutableArgumentMapping.Builder<>();
   }

   @Generated(from = "ArgumentMapping", generator = "Immutables")
   static final class Builder<S> {
      private static final long INIT_BIT_ARGUMENT_TYPE = 1L;
      private long initBits = 1L;
      private @NonNull ArgumentType<?> argumentType;
      private @NonNull SuggestionsType suggestionsType;
      private @Nullable SuggestionProvider<S> suggestionProvider;

      private Builder() {
      }

      public final ImmutableArgumentMapping.Builder<S> from(ArgumentMapping<S> instance) {
         Objects.requireNonNull(instance, "instance");
         this.argumentType(instance.argumentType());
         this.suggestionsType(instance.suggestionsType());
         SuggestionProvider<S> suggestionProviderValue = instance.suggestionProvider();
         if (suggestionProviderValue != null) {
            this.suggestionProvider(suggestionProviderValue);
         }

         return this;
      }

      public final ImmutableArgumentMapping.Builder<S> argumentType(@NonNull ArgumentType<?> argumentType) {
         this.argumentType = Objects.requireNonNull(argumentType, "argumentType");
         this.initBits &= -2L;
         return this;
      }

      public final ImmutableArgumentMapping.Builder<S> suggestionsType(@NonNull SuggestionsType suggestionsType) {
         this.suggestionsType = Objects.requireNonNull(suggestionsType, "suggestionsType");
         return this;
      }

      public final ImmutableArgumentMapping.Builder<S> suggestionProvider(@Nullable SuggestionProvider<S> suggestionProvider) {
         this.suggestionProvider = suggestionProvider;
         return this;
      }

      public ImmutableArgumentMapping<S> build() {
         if (this.initBits != 0L) {
            throw new IllegalStateException(this.formatRequiredAttributesMessage());
         } else {
            return new ImmutableArgumentMapping<>(this);
         }
      }

      private String formatRequiredAttributesMessage() {
         List<String> attributes = new ArrayList<>();
         if ((this.initBits & 1L) != 0L) {
            attributes.add("argumentType");
         }

         return "Cannot build ArgumentMapping, some of required attributes are not set " + attributes;
      }
   }
}
