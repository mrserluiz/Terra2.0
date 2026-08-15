package org.incendo.cloud;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.parser.aggregate.AggregateParserPairBuilder;
import org.incendo.cloud.parser.aggregate.AggregateParserTripletBuilder;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.parser.standard.LiteralParser;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PredicatePermission;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.type.tuple.Triplet;

@API(status = Status.STABLE)
public class Command<C> {
   private final List<@NonNull CommandComponent<C>> components;
   private final @Nullable CommandComponent<C> flagComponent;
   private final CommandExecutionHandler<C> commandExecutionHandler;
   private final Type senderType;
   private final Permission permission;
   private final CommandMeta commandMeta;
   private final CommandDescription commandDescription;

   @API(status = Status.INTERNAL)
   public Command(
      final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
      final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
      final @Nullable Type senderType,
      final @NonNull Permission permission,
      final @NonNull CommandMeta commandMeta,
      final @NonNull CommandDescription commandDescription
   ) {
      this.components = Objects.requireNonNull(commandComponents, "Command components may not be null");
      if (this.components.isEmpty()) {
         throw new IllegalArgumentException("At least one command component is required");
      }

      this.flagComponent = this.components.stream().filter(ca -> ca.type() == CommandComponent.ComponentType.FLAG).findFirst().orElse(null);
      boolean foundOptional = false;

      for (CommandComponent<C> component : this.components) {
         if (component.name().isEmpty()) {
            throw new IllegalArgumentException("Component names may not be empty");
         }

         if (foundOptional && component.required()) {
            throw new IllegalArgumentException(String.format("Command component '%s' cannot be placed after an optional argument", component.name()));
         }

         if (!component.required()) {
            foundOptional = true;
         }
      }

      this.commandExecutionHandler = commandExecutionHandler;
      this.senderType = senderType;
      this.permission = permission;
      this.commandMeta = commandMeta;
      this.commandDescription = commandDescription;
   }

   @API(status = Status.STABLE)
   public static <C> Command.@NonNull Builder<C> newBuilder(
      final @NonNull String commandName,
      final @NonNull CommandMeta commandMeta,
      final @NonNull Description description,
      final @NonNull String @NonNull ... aliases
   ) {
      List<CommandComponent<C>> commands = new ArrayList<>();
      ParserDescriptor<C, String> staticParser = LiteralParser.literal(commandName, aliases);
      commands.add(CommandComponent.builder(commandName, staticParser).description(description).build());
      return new Command.Builder<>(
         null,
         commandMeta,
         null,
         commands,
         CommandExecutionHandler.noOpCommandExecutionHandler(),
         Permission.empty(),
         Collections.emptyList(),
         CommandDescription.empty()
      );
   }

   public static <C> Command.@NonNull Builder<C> newBuilder(
      final @NonNull String commandName, final @NonNull CommandMeta commandMeta, final @NonNull String @NonNull ... aliases
   ) {
      List<CommandComponent<C>> commands = new ArrayList<>();
      ParserDescriptor<C, String> staticParser = LiteralParser.literal(commandName, aliases);
      commands.add(CommandComponent.<C, String>builder().name(commandName).parser(staticParser).build());
      return new Command.Builder<>(
         null,
         commandMeta,
         null,
         commands,
         CommandExecutionHandler.noOpCommandExecutionHandler(),
         Permission.empty(),
         Collections.emptyList(),
         CommandDescription.empty()
      );
   }

   @API(status = Status.STABLE)
   public @NonNull List<CommandComponent<C>> components() {
      return new ArrayList<>(this.components);
   }

   @API(status = Status.STABLE)
   public @NonNull CommandComponent<C> rootComponent() {
      return this.components.get(0);
   }

   @API(status = Status.EXPERIMENTAL)
   public @NonNull List<CommandComponent<C>> nonFlagArguments() {
      List<CommandComponent<C>> components = new ArrayList<>(this.components);
      if (this.flagComponent() != null) {
         components.remove(this.flagComponent());
      }

      return components;
   }

   @API(status = Status.STABLE)
   public @Nullable CommandComponent<C> flagComponent() {
      return this.flagComponent;
   }

   @API(status = Status.STABLE)
   public @Nullable CommandFlagParser<@NonNull C> flagParser() {
      CommandComponent<C> flagComponent = this.flagComponent();
      return flagComponent == null ? null : (CommandFlagParser)flagComponent.parser();
   }

   @API(status = Status.STABLE)
   public @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler() {
      return this.commandExecutionHandler;
   }

   @API(status = Status.STABLE)
   public @NonNull Optional<TypeToken<? extends C>> senderType() {
      return this.senderType == null ? Optional.empty() : Optional.of((TypeToken<? extends C>)TypeToken.get(this.senderType));
   }

   @API(status = Status.STABLE)
   public @NonNull Permission commandPermission() {
      return this.permission;
   }

   @API(status = Status.STABLE)
   public @NonNull CommandMeta commandMeta() {
      return this.commandMeta;
   }

   @API(status = Status.STABLE)
   public @NonNull CommandDescription commandDescription() {
      return this.commandDescription;
   }

   @Override
   public final String toString() {
      StringBuilder stringBuilder = new StringBuilder();

      for (CommandComponent<C> component : this.components()) {
         stringBuilder.append(component.name()).append(' ');
      }

      String build = stringBuilder.toString();
      return build.substring(0, build.length() - 1);
   }

   @API(status = Status.STABLE)
   public static final class Builder<C> {
      private final CommandMeta commandMeta;
      private final List<CommandComponent<C>> commandComponents;
      private final CommandExecutionHandler<C> commandExecutionHandler;
      private final Type senderType;
      private final Permission permission;
      private final CommandManager<C> commandManager;
      private final Collection<CommandFlag<?>> flags;
      private final CommandDescription commandDescription;

      private Builder(
         final @Nullable CommandManager<C> commandManager,
         final @NonNull CommandMeta commandMeta,
         final @Nullable Type senderType,
         final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
         final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
         final @NonNull Permission permission,
         final @NonNull Collection<CommandFlag<?>> flags,
         final @NonNull CommandDescription commandDescription
      ) {
         this.commandManager = commandManager;
         this.senderType = senderType;
         this.commandComponents = Objects.requireNonNull(commandComponents, "Components may not be null");
         this.commandExecutionHandler = Objects.requireNonNull(commandExecutionHandler, "Execution handler may not be null");
         this.permission = Objects.requireNonNull(permission, "Permission may not be null");
         this.commandMeta = Objects.requireNonNull(commandMeta, "Meta may not be null");
         this.flags = Objects.requireNonNull(flags, "Flags may not be null");
         this.commandDescription = Objects.requireNonNull(commandDescription, "Command description may not be null");
      }

      @API(status = Status.STABLE)
      public @Nullable TypeToken<? extends C> senderType() {
         return (TypeToken<? extends C>)(this.senderType == null ? null : TypeToken.get(this.senderType));
      }

      @API(status = Status.STABLE)
      public @NonNull Permission commandPermission() {
         return this.permission;
      }

      @API(status = Status.STABLE)
      public @NonNull CommandMeta meta() {
         return this.commandMeta;
      }

      @API(status = Status.STABLE)
      public Command.@NonNull Builder<@NonNull C> apply(final Command.Builder.@NonNull Applicable<@NonNull C> applicable) {
         return applicable.applyToCommandBuilder(this);
      }

      @API(status = Status.STABLE)
      public <V> Command.@NonNull Builder<C> meta(final @NonNull CloudKey<V> key, final @NonNull V value) {
         CommandMeta commandMeta = CommandMeta.builder().with(this.commandMeta).with(key, value).build();
         return new Command.Builder<>(
            this.commandManager,
            commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      public Command.@NonNull Builder<C> manager(final @Nullable CommandManager<C> commandManager) {
         return new Command.Builder<>(
            commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      @API(status = Status.STABLE)
      public Command.@NonNull Builder<C> commandDescription(final @NonNull CommandDescription commandDescription) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            commandDescription
         );
      }

      public @NonNull CommandDescription commandDescription() {
         return this.commandDescription;
      }

      public Command.@NonNull Builder<C> commandDescription(final @NonNull Description commandDescription) {
         return this.commandDescription(CommandDescription.commandDescription(commandDescription));
      }

      public Command.@NonNull Builder<C> commandDescription(final @NonNull Description commandDescription, final @NonNull Description verboseCommandDescription) {
         return this.commandDescription(CommandDescription.commandDescription(commandDescription, verboseCommandDescription));
      }

      public Command.@NonNull Builder<C> literal(final @NonNull String main, final @NonNull String... aliases) {
         return this.required(main, LiteralParser.literal(main, aliases));
      }

      @API(status = Status.STABLE)
      public Command.@NonNull Builder<C> literal(final @NonNull String main, final @NonNull Description description, final @NonNull String... aliases) {
         return this.required(main, LiteralParser.literal(main, aliases), description);
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(final @NonNull String name, final CommandComponent.@NonNull Builder<? super C, T> builder) {
         return this.argument(builder.name(name).required());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(final @NonNull String name, final CommandComponent.@NonNull Builder<? super C, T> builder) {
         return this.argument(builder.name(name).optional());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(final CommandComponent.@NonNull Builder<? super C, T> builder) {
         return this.argument(builder.required());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(final CommandComponent.@NonNull Builder<? super C, T> builder) {
         return this.argument(builder.optional());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(
         final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(
         final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(
         final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(
         final @NonNull CloudKey<T> name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull Description description,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(
         final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> required(
         final @NonNull String name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull Description description,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).optional().build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional().suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).optional().build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional().suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull String name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull Description description,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull CloudKey<T> name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull Description description,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull String name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull DefaultValue<? super C, T> defaultValue
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull String name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull DefaultValue<? super C, T> defaultValue,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<? super C, T> parser, final @NonNull DefaultValue<? super C, T> defaultValue
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull CloudKey<T> name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull DefaultValue<? super C, T> defaultValue,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull String name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull DefaultValue<? super C, T> defaultValue,
         final @NonNull Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull String name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull DefaultValue<? super C, T> defaultValue,
         final @NonNull Description description,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull CloudKey<T> name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull DefaultValue<? super C, T> defaultValue,
         final @NonNull Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).build());
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> optional(
         final @NonNull CloudKey<T> name,
         final @NonNull ParserDescriptor<? super C, T> parser,
         final @NonNull DefaultValue<? super C, T> defaultValue,
         final @NonNull Description description,
         final @NonNull SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).suggestionProvider(suggestions).build());
      }

      @API(status = Status.STABLE)
      public Command.@NonNull Builder<C> argument(final @NonNull CommandComponent<? super C> argument) {
         List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
         commandComponents.add((CommandComponent<C>)argument);
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      @API(status = Status.STABLE)
      public <T> Command.@NonNull Builder<C> argument(final CommandComponent.Builder<? super C, T> builder) {
         return this.commandManager != null ? this.argument(builder.commandManager(this.commandManager).build()) : this.argument(builder.build());
      }

      @API(status = Status.STABLE)
      public <U, V> Command.@NonNull Builder<C> requiredArgumentPair(
         final @NonNull String name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(status = Status.STABLE)
      public <U, V> Command.@NonNull Builder<C> requiredArgumentPair(
         final @NonNull CloudKey<Pair<U, V>> name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(status = Status.STABLE)
      public <U, V> Command.@NonNull Builder<C> optionalArgumentPair(
         final @NonNull String name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(status = Status.STABLE)
      public <U, V> Command.@NonNull Builder<C> optionalArgumentPair(
         final @NonNull CloudKey<Pair<U, V>> name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(status = Status.STABLE)
      public <U, V, O> Command.@NonNull Builder<C> requiredArgumentPair(
         final @NonNull String name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final AggregateParserPairBuilder.@NonNull Mapper<C, U, V, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, O> Command.@NonNull Builder<C> requiredArgumentPair(
         final @NonNull CloudKey<O> name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final AggregateParserPairBuilder.@NonNull Mapper<C, U, V, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, O> Command.@NonNull Builder<C> optionalArgumentPair(
         final @NonNull String name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final AggregateParserPairBuilder.@NonNull Mapper<C, U, V, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, O> Command.@NonNull Builder<C> optionalArgumentPair(
         final @NonNull CloudKey<O> name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final AggregateParserPairBuilder.@NonNull Mapper<C, U, V, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W> Command.@NonNull Builder<C> requiredArgumentTriplet(
         final @NonNull String name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W> Command.@NonNull Builder<C> requiredArgumentTriplet(
         final @NonNull CloudKey<Triplet<U, V, W>> name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W> Command.@NonNull Builder<C> optionalArgumentTriplet(
         final @NonNull String name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W> Command.@NonNull Builder<C> optionalArgumentTriplet(
         final @NonNull CloudKey<Triplet<U, V, W>> name,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W, O> Command.@NonNull Builder<C> requiredArgumentTriplet(
         final @NonNull String name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final AggregateParserTripletBuilder.@NonNull Mapper<C, U, V, W, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W, O> Command.@NonNull Builder<C> requiredArgumentTriplet(
         final @NonNull CloudKey<O> name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final AggregateParserTripletBuilder.@NonNull Mapper<C, U, V, W, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W, O> Command.@NonNull Builder<C> optionalArgumentTriplet(
         final @NonNull String name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final AggregateParserTripletBuilder.@NonNull Mapper<C, U, V, W, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      @API(status = Status.STABLE)
      public <U, V, W, O> Command.@NonNull Builder<C> optionalArgumentTriplet(
         final @NonNull CloudKey<O> name,
         final @NonNull TypeToken<O> outputType,
         final @NonNull String firstName,
         final @NonNull ParserDescriptor<C, U> firstParser,
         final @NonNull String secondName,
         final @NonNull ParserDescriptor<C, V> secondParser,
         final @NonNull String thirdName,
         final @NonNull ParserDescriptor<C, W> thirdParser,
         final AggregateParserTripletBuilder.@NonNull Mapper<C, U, V, W, O> mapper,
         final @NonNull Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      public Command.@NonNull Builder<C> handler(final @NonNull CommandExecutionHandler<C> commandExecutionHandler) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      public Command.@NonNull Builder<C> futureHandler(final CommandExecutionHandler.@NonNull FutureCommandExecutionHandler<C> commandExecutionHandler) {
         return this.handler(commandExecutionHandler);
      }

      @API(status = Status.STABLE)
      public @NonNull CommandExecutionHandler<C> handler() {
         return this.commandExecutionHandler;
      }

      @API(status = Status.STABLE)
      public Command.@NonNull Builder<C> prependHandler(final @NonNull CommandExecutionHandler<C> handler) {
         return this.handler(CommandExecutionHandler.delegatingExecutionHandler(Arrays.asList(handler, this.handler())));
      }

      @API(status = Status.STABLE)
      public Command.@NonNull Builder<C> appendHandler(final @NonNull CommandExecutionHandler<C> handler) {
         return this.handler(CommandExecutionHandler.delegatingExecutionHandler(Arrays.asList(this.handler(), handler)));
      }

      public <N extends C> Command.@NonNull Builder<N> senderType(final @NonNull Class<? extends N> senderType) {
         return this.senderType(TypeToken.get(senderType));
      }

      public <N extends C> Command.@NonNull Builder<N> senderType(final @NonNull TypeToken<? extends N> senderType) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            senderType.getType(),
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      public Command.@NonNull Builder<C> permission(final @NonNull Permission permission) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            permission,
            this.flags,
            this.commandDescription
         );
      }

      public Command.@NonNull Builder<C> permission(final @NonNull PredicatePermission<C> permission) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            permission,
            this.flags,
            this.commandDescription
         );
      }

      public Command.@NonNull Builder<C> permission(final @NonNull String permission) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            Permission.of(permission),
            this.flags,
            this.commandDescription
         );
      }

      public <N extends C> Command.@NonNull Builder<N> proxies(final @NonNull Command<N> command) {
         Command.Builder<N> builder;
         if (command.senderType().isPresent()) {
            builder = this.senderType(command.senderType().get());
         } else {
            builder = this;
         }

         for (CommandComponent<N> component : command.components()) {
            if (component.type() != CommandComponent.ComponentType.LITERAL) {
               builder = builder.argument(component);
            }
         }

         if (this.permission.permissionString().isEmpty()) {
            builder = builder.permission(command.commandPermission());
         }

         return builder.handler(command.commandExecutionHandler);
      }

      public <T> Command.@NonNull Builder<C> flag(final @NonNull CommandFlag<T> flag) {
         List<CommandFlag<?>> flags = new ArrayList<>(this.flags);
         flags.add(flag);
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            Collections.unmodifiableList(flags),
            this.commandDescription
         );
      }

      public <T> Command.@NonNull Builder<C> flag(final CommandFlag.@NonNull Builder<C, T> builder) {
         return this.flag(builder.build());
      }

      public @NonNull Command<C> build() {
         List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
         if (!this.flags.isEmpty()) {
            CommandFlagParser<C> flagParser = new CommandFlagParser<>(this.flags);
            CommandComponent<C> flagComponent = CommandComponent.<C, Object>builder()
               .name("flags")
               .parser(flagParser)
               .valueType(Object.class)
               .description(Description.of("Command flags"))
               .build();
            commandComponents.add(flagComponent);
         }

         return new Command<>(
            Collections.unmodifiableList(commandComponents),
            this.commandExecutionHandler,
            this.senderType,
            this.permission,
            this.commandMeta,
            this.commandDescription
         );
      }

      @API(status = Status.STABLE)
      @FunctionalInterface
      public interface Applicable<C> {
         @API(status = Status.STABLE)
         Command.@NonNull Builder<C> applyToCommandBuilder(Command.@NonNull Builder<C> builder);
      }
   }
}
