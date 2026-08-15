package org.incendo.cloud.component;

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.preprocessor.ComponentPreprocessor;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.key.CloudKeyHolder;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.STABLE)
public final class TypedCommandComponent<C, T> extends CommandComponent<C> implements CloudKeyHolder<T> {
   TypedCommandComponent(
      final @NonNull String name,
      final @NonNull ArgumentParser<C, ?> parser,
      final @NonNull TypeToken<?> valueType,
      final @NonNull Description description,
      final CommandComponent.@NonNull ComponentType componentType,
      final @Nullable DefaultValue<C, ?> defaultValue,
      final @NonNull SuggestionProvider<C> suggestionProvider,
      final @NonNull Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors
   ) {
      super(name, parser, valueType, description, componentType, defaultValue, suggestionProvider, componentPreprocessors);
   }

   @Override
   public @NonNull TypeToken<T> valueType() {
      return (TypeToken<T>)super.valueType();
   }

   @Override
   public @NonNull ArgumentParser<C, T> parser() {
      return (ArgumentParser<C, T>)super.parser();
   }

   @Override
   public @Nullable DefaultValue<C, T> defaultValue() {
      return (DefaultValue<C, T>)super.defaultValue();
   }

   @Override
   public @NonNull CloudKey<T> key() {
      return CloudKey.of(this.name(), this.valueType());
   }
}
