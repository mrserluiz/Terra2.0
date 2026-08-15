package org.incendo.cloud.context;

import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionRegistry;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.key.MutableCloudKeyContainer;
import org.incendo.cloud.parser.flag.FlagContext;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

@API(status = Status.STABLE)
public class CommandContext<C> implements MutableCloudKeyContainer {
   private final List<ParsingContext<C>> parsingContexts = new LinkedList<>();
   private final FlagContext flagContext = FlagContext.create();
   private final Map<CloudKey<?>, Object> internalStorage = new HashMap<>();
   private final C commandSender;
   private final boolean suggestions;
   private final CaptionRegistry<C> captionRegistry;
   private final CommandManager<C> commandManager;
   private volatile @MonotonicNonNull Command<C> currentCommand = null;

   @API(status = Status.STABLE)
   public CommandContext(final @NonNull C commandSender, final @NonNull CommandManager<C> commandManager) {
      this(false, commandSender, commandManager);
   }

   @API(status = Status.STABLE)
   public CommandContext(final boolean suggestions, final @NonNull C commandSender, final @NonNull CommandManager<C> commandManager) {
      this.commandSender = commandSender;
      this.suggestions = suggestions;
      this.commandManager = commandManager;
      this.captionRegistry = commandManager.captionRegistry();
   }

   public @NonNull String formatCaption(final @NonNull Caption caption, final @NonNull CaptionVariable @NonNull ... variables) {
      return this.formatCaption(this.commandManager.captionFormatter(), caption, variables);
   }

   public @NonNull String formatCaption(final @NonNull Caption caption, final @NonNull List<@NonNull CaptionVariable> variables) {
      return this.formatCaption(this.commandManager.captionFormatter(), caption, variables);
   }

   public <T> @NonNull T formatCaption(
      final @NonNull CaptionFormatter<C, T> formatter, final @NonNull Caption caption, final @NonNull CaptionVariable @NonNull ... variables
   ) {
      return formatter.formatCaption(caption, this.commandSender, this.captionRegistry.caption(caption, this.commandSender), variables);
   }

   public <T> @NonNull T formatCaption(
      final @NonNull CaptionFormatter<C, T> formatter, final @NonNull Caption caption, final @NonNull List<@NonNull CaptionVariable> variables
   ) {
      return formatter.formatCaption(caption, this.commandSender, this.captionRegistry.caption(caption, this.commandSender), variables);
   }

   @API(status = Status.STABLE)
   public @NonNull C sender() {
      return this.commandSender;
   }

   @API(status = Status.STABLE)
   public boolean hasPermission(final @NonNull Permission permission) {
      return this.commandManager.testPermission(this.commandSender, permission).allowed();
   }

   @API(status = Status.STABLE)
   public boolean hasPermission(final @NonNull String permission) {
      return this.commandManager.hasPermission(this.commandSender, permission);
   }

   public boolean isSuggestions() {
      return this.suggestions;
   }

   @Override
   public <T> void store(final @NonNull String key, final T value) {
      this.internalStorage.put(CloudKey.of(key), value);
   }

   @Override
   public <T> void store(final @NonNull CloudKey<T> key, final T value) {
      this.internalStorage.put(key, value);
   }

   @Override
   public boolean contains(final @NonNull CloudKey<?> key) {
      return this.internalStorage.containsKey(key);
   }

   @Override
   public <T> @NonNull Optional<T> optional(final @NonNull CloudKey<T> key) {
      Object value = this.internalStorage.get(key);
      if (value != null) {
         T castedValue = (T)value;
         return Optional.of(castedValue);
      } else {
         return Optional.empty();
      }
   }

   @Override
   public <T> @NonNull Optional<T> optional(final @NonNull String key) {
      Object value = this.internalStorage.get(CloudKey.of(key));
      if (value != null) {
         T castedValue = (T)value;
         return Optional.of(castedValue);
      } else {
         return Optional.empty();
      }
   }

   @Override
   public void remove(final @NonNull CloudKey<?> key) {
      this.internalStorage.remove(key);
   }

   @Override
   public <T> T computeIfAbsent(final @NonNull CloudKey<T> key, final @NonNull Function<CloudKey<T>, T> defaultFunction) {
      return (T)this.internalStorage.computeIfAbsent(key, k -> defaultFunction.apply((CloudKey<T>)k));
   }

   @API(status = Status.STABLE)
   public @NonNull CommandInput rawInput() {
      return this.getOrDefault("__raw_input__", CommandInput.empty()).copy();
   }

   @API(status = Status.MAINTAINED)
   public @NonNull ParsingContext<C> createParsingContext(final @NonNull CommandComponent<C> component) {
      ParsingContext<C> parsingContext = new ParsingContext<>(component);
      this.parsingContexts.add(parsingContext);
      return parsingContext;
   }

   @API(status = Status.MAINTAINED)
   public @NonNull ParsingContext<C> parsingContext(final @NonNull CommandComponent<C> component) {
      return this.parsingContexts.stream().filter(context -> context.component().equals(component)).findFirst().orElseThrow(NoSuchElementException::new);
   }

   @API(status = Status.MAINTAINED)
   public @NonNull ParsingContext<C> parsingContext(final int position) {
      return this.parsingContexts.get(position);
   }

   @API(status = Status.MAINTAINED)
   public @NonNull ParsingContext<C> parsingContext(final String name) {
      return this.parsingContexts.stream().filter(context -> context.component().name().equals(name)).findFirst().orElseThrow(NoSuchElementException::new);
   }

   @API(status = Status.MAINTAINED)
   public @NonNull List<@NonNull ParsingContext<@NonNull C>> parsingContexts() {
      return Collections.unmodifiableList(this.parsingContexts);
   }

   public @NonNull FlagContext flags() {
      return this.flagContext;
   }

   public @NonNull Command<C> command() {
      if (this.currentCommand == null) {
         throw new IllegalStateException(
            "The current command is only available once a command has been parsed. Mainly from execution handlers and post processors."
         );
      } else {
         return this.currentCommand;
      }
   }

   @API(status = Status.INTERNAL)
   public void command(final @NonNull Command<C> command) {
      this.currentCommand = Objects.requireNonNull(command, "command");
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Optional<T> inject(final @NonNull Class<T> clazz) {
      if (this.commandManager == null) {
         throw new UnsupportedOperationException("Cannot retrieve injectable values from a command context that is not associated with a command manager");
      } else {
         return this.commandManager.parameterInjectorRegistry().getInjectable(clazz, this, AnnotationAccessor.empty());
      }
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Optional<T> inject(final @NonNull TypeToken<T> type) {
      if (this.commandManager == null) {
         throw new UnsupportedOperationException("Cannot retrieve injectable values from a command context that is not associated with a command manager");
      } else {
         return this.commandManager.parameterInjectorRegistry().getInjectable(type, this, AnnotationAccessor.empty());
      }
   }

   @Override
   public final @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all() {
      return Collections.unmodifiableMap(this.internalStorage);
   }
}
