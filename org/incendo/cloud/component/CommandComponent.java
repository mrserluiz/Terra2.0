package org.incendo.cloud.component;

import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.preprocessor.ComponentPreprocessor;
import org.incendo.cloud.component.preprocessor.PreprocessorHolder;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.description.Describable;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.parser.standard.LiteralParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.STABLE)
public class CommandComponent<C> implements Comparable<CommandComponent<C>>, PreprocessorHolder<C>, Describable {
   private final String name;
   private final ArgumentParser<C, ?> parser;
   private final Description description;
   private final CommandComponent.ComponentType componentType;
   private final DefaultValue<C, ?> defaultValue;
   private final TypeToken<?> valueType;
   private final SuggestionProvider<C> suggestionProvider;
   private final Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors;

   public static <C, T> CommandComponent.@NonNull Builder<C, T> builder() {
      return new CommandComponent.Builder<>();
   }

   public static <C, T> CommandComponent.@NonNull Builder<C, T> builder(
      final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parserDescriptor
   ) {
      return builder().name(name).parser(parserDescriptor);
   }

   public static <C, T> CommandComponent.@NonNull Builder<C, T> builder(
      final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parserDescriptor
   ) {
      return builder().key(name).parser(parserDescriptor);
   }

   public static <C, T> CommandComponent.@NonNull Builder<C, T> ofType(final @NonNull Class<T> clazz, final @NonNull String name) {
      return builder().valueType(clazz).name(name);
   }

   CommandComponent(
      final @NonNull String name,
      final @NonNull ArgumentParser<C, ?> parser,
      final @NonNull TypeToken<?> valueType,
      final @NonNull Description description,
      final CommandComponent.@NonNull ComponentType componentType,
      final @Nullable DefaultValue<C, ?> defaultValue,
      final @NonNull SuggestionProvider<C> suggestionProvider,
      final @NonNull Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors
   ) {
      this.name = name;
      this.parser = parser;
      this.valueType = valueType;
      this.componentType = componentType;
      this.description = description;
      this.defaultValue = defaultValue;
      this.suggestionProvider = suggestionProvider;
      this.componentPreprocessors = new ArrayList<>(componentPreprocessors);
   }

   public @NonNull TypeToken<?> valueType() {
      return this.valueType;
   }

   public @NonNull ArgumentParser<C, ?> parser() {
      return this.parser;
   }

   public final @NonNull String name() {
      return this.name;
   }

   public final @NonNull Collection<@NonNull String> aliases() {
      return this.parser() instanceof LiteralParser ? ((LiteralParser)this.parser()).aliases() : Collections.emptyList();
   }

   public final @NonNull Collection<@NonNull String> alternativeAliases() {
      return this.parser() instanceof LiteralParser ? ((LiteralParser)this.parser()).alternativeAliases() : Collections.emptyList();
   }

   @Override
   public final @NonNull Description description() {
      return this.description;
   }

   public final boolean required() {
      return this.componentType.required();
   }

   public final boolean optional() {
      return this.componentType.optional();
   }

   public final CommandComponent.@NonNull ComponentType type() {
      return this.componentType;
   }

   public @Nullable DefaultValue<C, ?> defaultValue() {
      return this.defaultValue;
   }

   public final boolean hasDefaultValue() {
      return this.optional() && this.defaultValue() != null;
   }

   public final @NonNull SuggestionProvider<C> suggestionProvider() {
      return this.suggestionProvider;
   }

   public final @This @NonNull CommandComponent<C> addPreprocessor(final @NonNull ComponentPreprocessor<C> preprocessor) {
      this.componentPreprocessors.add(Objects.requireNonNull(preprocessor, "preprocessor"));
      return this;
   }

   public final @NonNull ArgumentParseResult<Boolean> preprocess(final @NonNull CommandContext<C> context, final @NonNull CommandInput input) {
      for (ComponentPreprocessor<C> preprocessor : this.componentPreprocessors) {
         ArgumentParseResult<Boolean> result = preprocessor.preprocess(context, input);
         if (result.failure().isPresent()) {
            return result;
         }
      }

      return ArgumentParseResult.success(true);
   }

   @Override
   public final @NonNull Collection<@NonNull ComponentPreprocessor<C>> preprocessors() {
      return Collections.unmodifiableCollection(this.componentPreprocessors);
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.name(), this.valueType());
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof CommandComponent)) {
         return false;
      }

      CommandComponent<?> that = (CommandComponent<?>)o;
      return this.name().equals(that.name()) && this.valueType().equals(that.valueType());
   }

   @Override
   public final @NonNull String toString() {
      return String.format(
         "%s{name=%s,type=%s,valueType=%s", this.getClass().getSimpleName(), this.name(), this.type(), this.valueType().getType().getTypeName()
      );
   }

   public final int compareTo(final @NonNull CommandComponent<C> other) {
      if (this.type() == CommandComponent.ComponentType.LITERAL) {
         return other.type() == CommandComponent.ComponentType.LITERAL ? this.name().compareTo(other.name()) : -1;
      } else {
         return other.type() == CommandComponent.ComponentType.LITERAL ? 1 : 0;
      }
   }

   @API(status = Status.STABLE)
   public static class Builder<C, T> {
      private CommandManager<C> commandManager;
      private String name;
      private ArgumentParser<C, T> parser;
      private Description description = Description.empty();
      private boolean required = true;
      private DefaultValue<C, ?> defaultValue;
      private TypeToken<T> valueType;
      private SuggestionProvider<C> suggestionProvider;
      private final Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors = new ArrayList<>();

      protected Builder() {
      }

      public CommandComponent.@This @NonNull Builder<C, T> commandManager(final @Nullable CommandManager<C> commandManager) {
         this.commandManager = commandManager;
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> key(final @NonNull CloudKey<T> cloudKey) {
         return this.name(cloudKey.name()).valueType(cloudKey.type());
      }

      public @MonotonicNonNull String name() {
         return this.name;
      }

      public CommandComponent.@This @NonNull Builder<C, T> name(final @NonNull String name) {
         this.name = Objects.requireNonNull(name, "name");
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> valueType(final @NonNull TypeToken<T> valueType) {
         this.valueType = Objects.requireNonNull(valueType, "valueType");
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> valueType(final @NonNull Class<T> valueType) {
         return this.valueType(TypeToken.get(valueType));
      }

      public @MonotonicNonNull ParserDescriptor<C, T> parser() {
         return this.valueType != null && this.parser != null ? ParserDescriptor.of(this.parser, this.valueType) : null;
      }

      public CommandComponent.@This @NonNull Builder<C, T> parser(final @NonNull ParserDescriptor<? super C, T> parserDescriptor) {
         return this.parser(parserDescriptor.parser()).valueType(parserDescriptor.valueType());
      }

      public @Nullable DefaultValue<C, T> defaultValue() {
         return (DefaultValue<C, T>)(this.defaultValue == null ? null : this.defaultValue);
      }

      public CommandComponent.@This @NonNull Builder<C, T> defaultValue(final @Nullable DefaultValue<? super C, T> defaultValue) {
         this.defaultValue = (DefaultValue<C, ?>)defaultValue;
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> required(final boolean required) {
         this.required = required;
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> required() {
         return this.required(true);
      }

      public CommandComponent.@This @NonNull Builder<C, T> optional() {
         return this.required(false);
      }

      public CommandComponent.@This @NonNull Builder<C, T> optional(final @Nullable DefaultValue<? super C, T> defaultValue) {
         return this.optional().defaultValue(defaultValue);
      }

      public @MonotonicNonNull Description description() {
         return this.description;
      }

      public CommandComponent.@This @NonNull Builder<C, T> description(final @NonNull Description description) {
         this.description = Objects.requireNonNull(description, "description");
         return this;
      }

      public @MonotonicNonNull SuggestionProvider<C> suggestionProvider() {
         return this.suggestionProvider;
      }

      public CommandComponent.@This @NonNull Builder<C, T> suggestionProvider(final @Nullable SuggestionProvider<? super C> suggestionProvider) {
         this.suggestionProvider = (SuggestionProvider<C>)suggestionProvider;
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> preprocessor(final @NonNull ComponentPreprocessor<? super C> preprocessor) {
         this.componentPreprocessors.add(Objects.requireNonNull((ComponentPreprocessor<C>)preprocessor, "preprocessor"));
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> preprocessors(final @NonNull Collection<ComponentPreprocessor<C>> preprocessors) {
         this.componentPreprocessors.addAll(preprocessors);
         return this;
      }

      public CommandComponent.@This @NonNull Builder<C, T> parser(final @NonNull ArgumentParser<? super C, T> parser) {
         this.parser = Objects.requireNonNull((ArgumentParser<C, T>)parser, "parser");
         return this;
      }

      public @NonNull TypedCommandComponent<C, T> build() {
         ArgumentParser<C, T> parser = null;
         if (this.parser != null) {
            parser = this.parser;
         } else if (this.commandManager != null) {
            parser = this.commandManager.parserRegistry().createParser(this.valueType, ParserParameters.empty()).orElse(null);
         }

         if (parser == null) {
            parser = (ctx, input) -> ArgumentParseResult.failure(new UnsupportedOperationException("No parser was specified"));
         }

         CommandComponent.ComponentType componentType;
         if (this.parser instanceof LiteralParser) {
            componentType = CommandComponent.ComponentType.LITERAL;
         } else if (this.parser instanceof CommandFlagParser) {
            componentType = CommandComponent.ComponentType.FLAG;
         } else if (this.required) {
            componentType = CommandComponent.ComponentType.REQUIRED_VARIABLE;
         } else {
            componentType = CommandComponent.ComponentType.OPTIONAL_VARIABLE;
         }

         SuggestionProvider<C> suggestionProvider;
         if (this.suggestionProvider == null) {
            suggestionProvider = parser.suggestionProvider();
         } else {
            suggestionProvider = this.suggestionProvider;
         }

         return new TypedCommandComponent<>(
            Objects.requireNonNull(this.name, "name"),
            parser,
            Objects.requireNonNull(this.valueType, "valueType"),
            Objects.requireNonNull(this.description, "description"),
            componentType,
            this.defaultValue,
            suggestionProvider,
            Objects.requireNonNull(this.componentPreprocessors, "componentPreprocessors")
         );
      }
   }

   @API(status = Status.STABLE)
   public enum ComponentType {
      LITERAL(true),
      REQUIRED_VARIABLE(true),
      OPTIONAL_VARIABLE(false),
      FLAG(false);

      private final boolean required;

      ComponentType(final boolean required) {
         this.required = required;
      }

      public boolean required() {
         return this.required;
      }

      public boolean optional() {
         return !this.required;
      }
   }
}
