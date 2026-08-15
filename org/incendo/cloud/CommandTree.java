package org.incendo.cloud;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.context.ParsingContext;
import org.incendo.cloud.exception.AmbiguousNodeException;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoCommandInLeafException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.SuggestionContext;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.parser.standard.LiteralParser;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.setting.ManagerSetting;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.util.CompletableFutures;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandTree<C> {
   private final Object commandLock = new Object();
   private final CommandNode<C> internalTree = new CommandNode<>(null);
   private final CommandManager<C> commandManager;

   private CommandTree(final @NonNull CommandManager<C> commandManager) {
      this.commandManager = commandManager;
   }

   public static <C> @NonNull CommandTree<C> newTree(final @NonNull CommandManager<C> commandManager) {
      return new CommandTree<>(commandManager);
   }

   @API(status = Status.STABLE)
   public @NonNull CommandManager<C> commandManager() {
      return this.commandManager;
   }

   @API(status = Status.STABLE)
   public @NonNull Collection<@NonNull CommandNode<C>> rootNodes() {
      return this.internalTree.children();
   }

   public @Nullable CommandNode<C> getNamedNode(final @Nullable String name) {
      for (CommandNode<C> node : this.rootNodes()) {
         CommandComponent<C> component = node.component();
         if (component != null && component.type() == CommandComponent.ComponentType.LITERAL) {
            for (String alias : component.aliases()) {
               if (alias.equalsIgnoreCase(name)) {
                  return node;
               }
            }
         }
      }

      return null;
   }

   @API(status = Status.STABLE)
   public @NonNull CompletableFuture<@Nullable Command<C>> parse(
      final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput, final @NonNull Executor parsingExecutor
   ) {
      return CompletableFutures.<Command<C>>scheduleOn(parsingExecutor, () -> this.parseDirect(commandContext, commandInput, parsingExecutor))
         .thenApply(command -> {
            if (command != null) {
               commandContext.command((Command<C>)command);
            }

            return command;
         });
   }

   private @NonNull CompletableFuture<@Nullable Command<C>> parseDirect(
      final @NonNull CommandContext<C> commandContext, final @NonNull CommandInput commandInput, final @NonNull Executor parsingExecutor
   ) {
      return this.internalTree.isLeaf() && this.internalTree.component() == null
         ? CompletableFutures.failedFuture(new NoSuchCommandException(commandContext.sender(), new ArrayList<>(), commandInput.peekString()))
         : this.parseCommand(new ArrayList<>(), commandContext, commandInput, this.internalTree, parsingExecutor)
            .thenCompose(
               command -> command != null
                     && command.senderType().isPresent()
                     && !GenericTypeReflector.isSuperType(command.senderType().get().getType(), commandContext.sender().getClass())
                  ? CompletableFutures.failedFuture(
                     new InvalidCommandSenderException(
                        commandContext.sender(), command.senderType().get().getType(), new ArrayList<>(command.components()), (Command<?>)command
                     )
                  )
                  : CompletableFuture.completedFuture((Command<C>)command)
            );
   }

   private CompletableFuture<Command<C>> parseCommand(
      final List<CommandComponent<C>> parsedArguments,
      final CommandContext<C> commandContext,
      final CommandInput commandInput,
      final CommandNode<C> root,
      final Executor executor
   ) {
      Optional<PermissionResult> permissionResult = this.determineAccess(commandContext.sender(), root);
      if (!permissionResult.isPresent()) {
         return CompletableFutures.failedFuture(
            new InvalidCommandSenderException(
               commandContext.sender(), root.nodeMeta().get(CommandNode.META_KEY_SENDER_TYPES), this.getComponentChain(root), null
            )
         );
      }

      if (permissionResult.get().denied()) {
         return CompletableFutures.failedFuture(new NoPermissionException(permissionResult.get(), commandContext.sender(), this.getComponentChain(root)));
      }

      CompletableFuture<Command<C>> parsedChild = this.attemptParseUnambiguousChild(parsedArguments, commandContext, root, commandInput, executor);
      if (parsedChild != null) {
         return parsedChild;
      }

      if (root.children().isEmpty()) {
         CommandComponent<C> rootComponent = root.component();
         return rootComponent != null && root.command() != null && commandInput.isEmpty()
            ? CompletableFuture.completedFuture(root.command())
            : CompletableFutures.failedFuture(
               new InvalidSyntaxException(
                  this.commandManager.commandSyntaxFormatter().apply(commandContext.sender(), parsedArguments, root),
                  commandContext.sender(),
                  this.getComponentChain(root)
               )
            );
      }

      CompletableFuture<Command<C>> childCompletable = CompletableFuture.completedFuture(null);

      for (CommandNode<C> child : new ArrayList<>(root.children())) {
         if (child.component() != null) {
            childCompletable = childCompletable.thenCompose(previousResult -> {
               if (previousResult != null) {
                  return CompletableFuture.completedFuture((Command<C>)previousResult);
               }

               CommandComponent<C> component = Objects.requireNonNull(child.component());
               ParsingContext<C> parsingContext = commandContext.createParsingContext(component);
               commandInput.skipWhitespace(1);
               CommandInput currentInput = commandInput.copy();
               parsingContext.markStart();
               return component.parser().parseFuture(commandContext, commandInput).thenComposeAsync(result -> {
                  parsingContext.markEnd();
                  parsingContext.success(!result.failure().isPresent());
                  parsingContext.consumedInput(currentInput, commandInput);
                  if (result.parsedValue().isPresent()) {
                     parsedArguments.add(component);
                     return this.parseCommand(parsedArguments, commandContext, commandInput, child, executor);
                  }

                  if (result.failure().isPresent()) {
                     commandInput.cursor(currentInput.cursor());
                  }

                  return CompletableFuture.completedFuture(null);
               }, executor);
            });
         }
      }

      return childCompletable.thenCompose(
         completedCommand -> {
            if (completedCommand != null) {
               return CompletableFuture.completedFuture((Command<C>)completedCommand);
            } else if (root.equals(this.internalTree)) {
               return CompletableFutures.failedFuture(
                  new NoSuchCommandException(
                     commandContext.sender(), this.getChain(root).stream().map(CommandNode::component).collect(Collectors.toList()), commandInput.peekString()
                  )
               );
            } else {
               CommandComponent<C> rootComponent = root.component();
               if (rootComponent != null && root.command() != null && commandInput.isEmpty()) {
                  Command<C> command = root.command();
                  PermissionResult check = this.commandManager.testPermission(commandContext.sender(), command.commandPermission());
                  return check.denied()
                     ? CompletableFutures.failedFuture(new NoPermissionException(check, commandContext.sender(), this.getComponentChain(root)))
                     : CompletableFuture.completedFuture(root.command());
               } else {
                  return CompletableFutures.failedFuture(
                     new InvalidSyntaxException(
                        this.commandManager.commandSyntaxFormatter().apply(commandContext.sender(), parsedArguments, root),
                        commandContext.sender(),
                        this.getComponentChain(root)
                     )
                  );
               }
            }
         }
      );
   }

   private @Nullable CompletableFuture<@Nullable Command<C>> attemptParseUnambiguousChild(
      final @NonNull List<@NonNull CommandComponent<C>> parsedArguments,
      final @NonNull CommandContext<C> commandContext,
      final @NonNull CommandNode<C> root,
      final @NonNull CommandInput commandInput,
      final @NonNull Executor executor
   ) {
      C sender = commandContext.sender();
      List<CommandNode<C>> children = root.children();
      if (!commandInput.isEmpty() && this.matchesLiteral(children, commandInput.peekString())) {
         return null;
      }

      List<CommandNode<C>> argumentNodes = children.stream()
         .filter(n -> n.component() != null && n.component().type() != CommandComponent.ComponentType.LITERAL)
         .collect(Collectors.toList());
      if (argumentNodes.size() > 1) {
         throw new IllegalStateException("Unexpected ambiguity detected, number of dynamic child nodes should not exceed 1");
      }

      if (argumentNodes.isEmpty()) {
         return null;
      }

      CommandNode<C> child = argumentNodes.get(0);
      Optional<PermissionResult> childCheck = this.determineAccess(sender, child);
      if (!childCheck.isPresent()) {
         return CompletableFutures.failedFuture(
            new InvalidCommandSenderException(sender, child.nodeMeta().get(CommandNode.META_KEY_SENDER_TYPES), this.getComponentChain(child), null)
         );
      }

      if (!commandInput.isEmpty() && childCheck.get().denied()) {
         return CompletableFutures.failedFuture(new NoPermissionException(childCheck.get(), sender, this.getComponentChain(child)));
      }

      if (child.component() == null) {
         return null;
      }

      ArgumentParseResult<?> argumentValue = null;
      if (commandInput.isEmpty() && child.component().type() != CommandComponent.ComponentType.FLAG) {
         CommandComponent<C> childComponent = Objects.requireNonNull(child.component());
         if (!childComponent.hasDefaultValue()) {
            if (child.component().required()) {
               if (child.isLeaf()) {
                  CommandComponent<C> rootComponent = root.component();
                  if (rootComponent != null && root.command() != null) {
                     Command<C> command = root.command();
                     PermissionResult check = this.commandManager().testPermission(sender, command.commandPermission());
                     if (check.allowed()) {
                        return CompletableFuture.completedFuture(command);
                     }

                     return CompletableFutures.failedFuture(new NoPermissionException(check, sender, this.getComponentChain(root)));
                  }

                  List<CommandComponent<C>> components = Objects.requireNonNull(child.command()).components();
                  return CompletableFutures.failedFuture(
                     new InvalidSyntaxException(
                        this.commandManager.commandSyntaxFormatter().apply(commandContext.sender(), components, child), sender, this.getComponentChain(root)
                     )
                  );
               }

               CommandComponent<C> rootComponent = root.component();
               if (rootComponent != null && root.command() != null) {
                  Command<C> command = Objects.requireNonNull(root.command());
                  PermissionResult check = this.commandManager().testPermission(sender, command.commandPermission());
                  if (check.allowed()) {
                     return CompletableFuture.completedFuture(command);
                  }

                  return CompletableFutures.failedFuture(new NoPermissionException(check, sender, this.getComponentChain(root)));
               }

               return CompletableFutures.failedFuture(
                  new InvalidSyntaxException(
                     this.commandManager.commandSyntaxFormatter().apply(commandContext.sender(), parsedArguments, root), sender, this.getComponentChain(root)
                  )
               );
            }

            if (child.command() == null) {
               CommandNode<C> node = child;

               while (!node.isLeaf()) {
                  node = node.children().get(0);
                  CommandComponent<C> nodeComponent = node.component();
                  if (nodeComponent != null && node.command() != null) {
                     child.command(node.command());
                  }
               }
            }

            return CompletableFuture.completedFuture(child.command());
         }

         DefaultValue<C, ?> defaultValue = Objects.requireNonNull(childComponent.defaultValue(), "defaultValue");
         if (defaultValue instanceof DefaultValue.ParsedDefaultValue) {
            return this.attemptParseUnambiguousChild(
               parsedArguments, commandContext, root, commandInput.appendString(((DefaultValue.ParsedDefaultValue)defaultValue).value()), executor
            );
         }

         argumentValue = defaultValue.evaluateDefault(commandContext);
      }

      CommandComponent<C> component = Objects.requireNonNull(child.component());
      CompletableFuture<?> parseResult;
      if (argumentValue != null) {
         if (argumentValue.parsedValue().isPresent()) {
            parseResult = CompletableFuture.completedFuture(argumentValue.parsedValue().get());
         } else {
            parseResult = CompletableFutures.failedFuture(this.argumentParseException(commandContext, child, argumentValue));
         }
      } else {
         parseResult = this.parseArgument(commandContext, child, commandInput, executor).thenApply(result -> result.parsedValue().orElse(null));
      }

      return parseResult.thenComposeAsync(
         value -> {
            if (value == null) {
               return CompletableFuture.completedFuture(null);
            }

            commandContext.store(component.name(), value);
            if (child.isLeaf()) {
               return commandInput.isEmpty()
                  ? CompletableFuture.completedFuture(child.command())
                  : CompletableFutures.failedFuture(
                     new InvalidSyntaxException(
                        this.commandManager.commandSyntaxFormatter().apply(commandContext.sender(), parsedArguments, child),
                        sender,
                        this.getComponentChain(root)
                     )
                  );
            }

            parsedArguments.add(Objects.requireNonNull(child.component()));
            return this.parseCommand(parsedArguments, commandContext, commandInput, child, executor);
         },
         executor
      );
   }

   private boolean matchesLiteral(final @NonNull List<@NonNull CommandNode<C>> children, final @NonNull String input) {
      return children.stream()
         .map(CommandNode::component)
         .filter(Objects::nonNull)
         .filter(n -> n.type() == CommandComponent.ComponentType.LITERAL)
         .flatMap(arg -> Stream.concat(Stream.of(arg.name()), arg.aliases().stream()))
         .anyMatch(arg -> arg.equals(input));
   }

   private @NonNull CompletableFuture<ArgumentParseResult<?>> parseArgument(
      final @NonNull CommandContext<C> commandContext,
      final @NonNull CommandNode<C> node,
      final @NonNull CommandInput commandInput,
      final @NonNull Executor executor
   ) {
      ParsingContext<C> parsingContext = commandContext.createParsingContext(node.component());
      parsingContext.markStart();
      ArgumentParseResult<Boolean> preParseResult = node.component().preprocess(commandContext, commandInput);
      if (!preParseResult.failure().isPresent() && preParseResult.parsedValue().orElse(false)) {
         commandInput.skipWhitespace(1);
         CommandInput currentInput = commandInput.copy();
         return node.component().parser().parseFuture(commandContext, commandInput).thenComposeAsync(result -> {
            parsingContext.consumedInput(currentInput, commandInput);
            parsingContext.markEnd();
            parsingContext.success(false);
            if (result.failure().isPresent()) {
               commandInput.cursor(currentInput.cursor());
               return CompletableFutures.failedFuture(this.argumentParseException(commandContext, node, (ArgumentParseResult<?>)result));
            } else {
               return CompletableFuture.completedFuture((ArgumentParseResult<?>)result);
            }
         }, executor);
      } else {
         parsingContext.markEnd();
         parsingContext.success(false);
         return preParseResult.failure().isPresent()
            ? CompletableFutures.failedFuture(this.argumentParseException(commandContext, node, preParseResult))
            : CompletableFuture.completedFuture(preParseResult);
      }
   }

   private @NonNull ArgumentParseException argumentParseException(
      final CommandContext<C> commandContext, final CommandNode<C> node, final ArgumentParseResult<?> result
   ) {
      return new ArgumentParseException(result.failure().get(), commandContext.sender(), this.getComponentChain(node));
   }

   @API(status = Status.STABLE)
   public <S extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> getSuggestions(
      final @NonNull CommandContext<C> context,
      final @NonNull CommandInput commandInput,
      final @NonNull SuggestionMapper<S> mapper,
      final @NonNull Executor executor
   ) {
      return CompletableFutures.scheduleOn(executor, () -> this.getSuggestionsDirect(context, commandInput, mapper, executor));
   }

   private <S extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> getSuggestionsDirect(
      final @NonNull CommandContext<C> context,
      final @NonNull CommandInput commandInput,
      final @NonNull SuggestionMapper<S> mapper,
      final @NonNull Executor executor
   ) {
      SuggestionContext<C, S> suggestionCtx = new SuggestionContext<>(this.commandManager.suggestionProcessor(), context, commandInput, mapper);
      return this.getSuggestions(suggestionCtx, commandInput, this.internalTree, executor).thenApply($ -> suggestionCtx.makeSuggestions());
   }

   private @NonNull CompletableFuture<SuggestionContext<C, ?>> getSuggestions(
      final @NonNull SuggestionContext<C, ?> context,
      final @NonNull CommandInput commandInput,
      final @NonNull CommandNode<C> root,
      final @NonNull Executor executor
   ) {
      if (!this.determineAccess(context.commandContext().sender(), root).map(PermissionResult::allowed).orElse(false)) {
         return CompletableFuture.completedFuture(context);
      }

      List<CommandNode<C>> children = root.children();
      List<CommandNode<C>> staticArguments = children.stream()
         .filter(n -> n.component() != null)
         .filter(n -> n.component().type() == CommandComponent.ComponentType.LITERAL)
         .collect(Collectors.toList());
      if (!commandInput.isEmpty()) {
         commandInput.skipWhitespace(1);
      }

      if (!staticArguments.isEmpty() && !commandInput.isEmpty(true)) {
         CommandInput commandInputCopy = commandInput.copy();

         for (CommandNode<C> child : staticArguments) {
            CommandComponent<C> childComponent = child.component();
            if (childComponent != null) {
               ArgumentParseResult<?> result = childComponent.parser().parse(context.commandContext(), commandInput);
               if (result.failure().isPresent()) {
                  commandInput.cursor(commandInputCopy.cursor());
               }

               if (result.parsedValue().isPresent()) {
                  if (!commandInput.isEmpty()) {
                     return this.getSuggestions(context, commandInput, child, executor);
                  }
                  break;
               }
            }
         }

         commandInput.cursor(commandInputCopy.cursor());
      }

      CompletableFuture<SuggestionContext<C, ?>> suggestionFuture = CompletableFuture.completedFuture(context);
      if (commandInput.remainingTokens() <= 1) {
         for (CommandNode<C> node : staticArguments) {
            suggestionFuture = suggestionFuture.thenCompose(ctx -> this.addSuggestionsForLiteralArgument(context, node, commandInput));
         }
      }

      for (CommandNode<C> child : root.children()) {
         if (child.component() != null && child.component().type() != CommandComponent.ComponentType.LITERAL) {
            suggestionFuture = suggestionFuture.thenCompose(ctx -> this.addSuggestionsForDynamicArgument(context, commandInput, child, executor, false));
         }
      }

      return suggestionFuture;
   }

   private CompletableFuture<SuggestionContext<C, ?>> addSuggestionsForLiteralArgument(
      final @NonNull SuggestionContext<C, ?> context, final @NonNull CommandNode<C> node, final @NonNull CommandInput input
   ) {
      if (!this.determineAccess(context.commandContext().sender(), node).map(PermissionResult::allowed).orElse(false)) {
         return CompletableFuture.completedFuture(context);
      }

      CommandComponent<C> component = Objects.requireNonNull(node.component());
      return component.suggestionProvider().suggestionsFuture(context.commandContext(), input.copy()).thenApply(suggestionsToAdd -> {
         String string = input.peekString();

         for (Suggestion suggestion : suggestionsToAdd) {
            if (!suggestion.suggestion().equals(string) && suggestion.suggestion().startsWith(string)) {
               context.addSuggestion(suggestion);
            }
         }

         return context;
      });
   }

   private @NonNull CompletableFuture<SuggestionContext<C, ?>> addSuggestionsForDynamicArgument(
      final @NonNull SuggestionContext<C, ?> context,
      final @NonNull CommandInput commandInput,
      final @NonNull CommandNode<C> child,
      final @NonNull Executor executor,
      final boolean inFlag
   ) {
      CommandComponent<C> component = child.component();
      if (component == null) {
         return CompletableFuture.completedFuture(context);
      }

      if (!inFlag && component.parser() instanceof CommandFlagParser) {
         CommandFlagParser<C> parser = (CommandFlagParser<C>)component.parser();
         return parser.parseCurrentFlag(context.commandContext(), commandInput, executor).thenCompose(lastFlag -> {
            if (lastFlag.isPresent()) {
               context.commandContext().store(CommandFlagParser.FLAG_META_KEY, lastFlag.get());
            } else {
               context.commandContext().remove(CommandFlagParser.FLAG_META_KEY);
            }

            return this.addSuggestionsForDynamicArgument(context, commandInput, child, executor, true);
         });
      }

      if (!commandInput.isEmpty()
         && commandInput.remainingTokens() != 1
         && (!child.isLeaf() || !(child.component().parser() instanceof AggregateParser))
         && (!child.isLeaf() || !(child.component().parser() instanceof CommandFlagParser))) {
         CommandInput commandInputOriginal = commandInput.copy();
         ArgumentParseResult<Boolean> preParseResult = component.preprocess(context.commandContext(), commandInput);
         boolean preParseSuccess = !preParseResult.failure().isPresent() && preParseResult.parsedValue().orElse(false);
         CompletableFuture<SuggestionContext<C, ?>> parsingFuture;
         if (!preParseSuccess) {
            parsingFuture = CompletableFuture.completedFuture(null);
         } else {
            ParsingContext<C> parsingContext = context.commandContext().createParsingContext(child.component());
            parsingContext.markStart();
            CommandInput preParseInput = commandInput.copy();
            parsingFuture = child.component().parser().parseFuture(context.commandContext(), commandInput).thenComposeAsync(result -> {
               Optional<?> parsedValue = result.parsedValue();
               boolean parseSuccess = parsedValue.isPresent();
               if (result.failure().isPresent()) {
                  commandInput.cursor(preParseInput.cursor());
                  return this.addArgumentSuggestions(context, child, commandInput, executor);
               }

               if (child.isLeaf()) {
                  if (!commandInput.isEmpty()) {
                     return CompletableFuture.completedFuture(context);
                  }

                  commandInput.cursor(commandInputOriginal.cursor());
                  this.addArgumentSuggestions(context, child, commandInput, executor);
               }

               if (!parseSuccess || commandInput.isEmpty() && !commandInput.input().endsWith(" ")) {
                  if (!parseSuccess && commandInputOriginal.remainingTokens() > 1) {
                     commandInput.cursor(commandInputOriginal.cursor());
                     return CompletableFuture.completedFuture(context);
                  } else {
                     return CompletableFuture.completedFuture(null);
                  }
               } else {
                  if (commandInput.isEmpty()) {
                     commandInput.moveCursor(-1);
                  }

                  context.commandContext().store(child.component().name(), parsedValue.get());
                  parsingContext.success(true);
                  return this.getSuggestions(context, commandInput, child, executor);
               }
            }, executor);
         }

         return parsingFuture.thenCompose(
            previousResult -> {
               if (previousResult != null) {
                  return CompletableFuture.completedFuture((SuggestionContext<C, ?>)previousResult);
               }

               commandInput.cursor(commandInputOriginal.cursor());
               return !preParseSuccess && commandInput.remainingTokens() > 1
                  ? CompletableFuture.completedFuture(context)
                  : this.addArgumentSuggestions(context, child, commandInput, executor);
            }
         );
      } else {
         return this.addArgumentSuggestions(context, child, commandInput, executor);
      }
   }

   private @NonNull CompletableFuture<SuggestionContext<C, ?>> addArgumentSuggestions(
      final @NonNull SuggestionContext<C, ?> context, final @NonNull CommandNode<C> node, final @NonNull CommandInput input, final @NonNull Executor executor
   ) {
      CommandComponent<C> component = Objects.requireNonNull(node.component());
      return this.addArgumentSuggestions(context, component, input, executor)
         .thenCompose(
            ctx -> {
               boolean isParsingFlag = component.type() == CommandComponent.ComponentType.FLAG
                  && !node.children().isEmpty()
                  && (!input.hasRemainingInput() || input.peek() != '-')
                  && !context.commandContext().optional(CommandFlagParser.FLAG_META_KEY).isPresent();
               return !isParsingFlag
                  ? CompletableFuture.completedFuture((SuggestionContext<C, ?>)ctx)
                  : CompletableFuture.allOf(
                        node.children()
                           .stream()
                           .map(child -> this.addArgumentSuggestions(context, Objects.requireNonNull(child.component()), input, executor))
                           .toArray(CompletableFuture[]::new)
                     )
                     .thenApply(v -> ctx);
            }
         );
   }

   private CompletableFuture<SuggestionContext<C, ?>> addArgumentSuggestions(
      final @NonNull SuggestionContext<C, ?> context,
      final @NonNull CommandComponent<C> component,
      final @NonNull CommandInput input,
      final @NonNull Executor executor
   ) {
      return component.suggestionProvider()
         .suggestionsFuture(context.commandContext(), input.copy())
         .thenAcceptAsync(context::addSuggestions, executor)
         .thenApply(in -> context);
   }

   public void insertCommand(final @NonNull Command<C> command) {
      synchronized (this.commandLock) {
         CommandComponent<C> flagComponent = command.flagComponent();
         List<CommandComponent<C>> nonFlagArguments = command.nonFlagArguments();
         int flagStartIdx = this.flagStartIndex(nonFlagArguments);
         CommandNode<C> node = this.internalTree;

         for (int i = 0; i < nonFlagArguments.size(); i++) {
            CommandComponent<C> component = nonFlagArguments.get(i);
            CommandNode<C> tempNode = node.getChild(component);
            if (tempNode == null) {
               tempNode = node.addChild(component);
            } else if (component.type() == CommandComponent.ComponentType.LITERAL && tempNode.component() != null) {
               for (String alias : component.aliases()) {
                  ((LiteralParser)tempNode.component().parser()).insertAlias(alias);
               }
            }

            if (!node.children().isEmpty()) {
               node.sortChildren();
            }

            tempNode.parent(node);
            node = tempNode;
            if (flagComponent != null && i >= flagStartIdx) {
               tempNode = node.addChild(flagComponent);
               tempNode.parent(node);
               node = tempNode;
            }
         }

         CommandComponent<C> nodeComponent = node.component();
         if (nodeComponent != null) {
            if (node.command() != null) {
               throw new IllegalStateException(
                  String.format("Duplicate command chains detected. Node '%s' already has an owning command (%s)", node, node.command())
               );
            }

            node.command(command);
         }

         this.verifyAndRegister();
      }
   }

   private int flagStartIndex(final @NonNull List<CommandComponent<C>> components) {
      if (this.commandManager.settings().get(ManagerSetting.LIBERAL_FLAG_PARSING)) {
         for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).type() == CommandComponent.ComponentType.LITERAL) {
               return i;
            }
         }
      }

      return components.size() - 1;
   }

   private Optional<PermissionResult> determineAccess(final @NonNull C sender, final @NonNull CommandNode<C> node) {
      Map<Type, Permission> accessMap = node.nodeMeta().getOrNull(CommandNode.META_KEY_ACCESS);
      if (accessMap == null) {
         throw new IllegalStateException("Expected access requirements to be propagated");
      }

      Set<Permission> failed = new HashSet<>();

      for (Entry<Type, Permission> entry : accessMap.entrySet()) {
         if (GenericTypeReflector.isSuperType(entry.getKey(), sender.getClass())) {
            PermissionResult result = this.commandManager.testPermission(sender, entry.getValue());
            if (result.allowed()) {
               return Optional.of(result);
            }

            failed.add(entry.getValue());
         }
      }

      return failed.isEmpty() ? Optional.empty() : Optional.of(PermissionResult.denied(Permission.anyOf(failed)));
   }

   private void verifyAndRegister() {
      this.internalTree.children().stream().map(CommandNode::component).forEach(component -> {
         if (component.type() != CommandComponent.ComponentType.LITERAL) {
            throw new IllegalStateException("Top level command argument cannot be a variable");
         }
      });
      this.checkAmbiguity(this.internalTree);
      this.getLeaves(this.internalTree).forEach(leaf -> {
         if (leaf.command() == null) {
            throw new NoCommandInLeafException(leaf.component());
         }

         Command<C> owningCommand = leaf.command();
         this.commandManager.commandRegistrationHandler().registerCommand(owningCommand);
      });
      this.getExecutorNodes(this.internalTree).forEach(this::propagateRequirements);
   }

   @API(status = Status.INTERNAL)
   public @NonNull CommandNode<C> rootNode() {
      return this.internalTree;
   }

   private void propagateRequirements(final @NonNull CommandNode<C> leafNode) {
      Permission commandPermission = leafNode.command().commandPermission();
      Type senderType = leafNode.command().senderType().map(TypeToken::getType).orElse(null);
      if (senderType == null) {
         senderType = Object.class;
      }

      List<CommandNode<C>> chain = this.getChain(leafNode);
      Collections.reverse(chain);

      for (CommandNode<C> commandArgumentNode : chain) {
         Set<Type> senderTypes = commandArgumentNode.nodeMeta().computeIfAbsent(CommandNode.META_KEY_SENDER_TYPES, $ -> new HashSet<>());
         updateSenderRequirements(senderTypes, senderType);
         Map<Type, Permission> accessMap = commandArgumentNode.nodeMeta().computeIfAbsent(CommandNode.META_KEY_ACCESS, $ -> new HashMap<>());
         updateAccess(accessMap, senderType, commandPermission);
      }
   }

   private static void updateAccess(final Map<Type, Permission> senderTypes, final Type senderType, final Permission commandPermission) {
      senderTypes.compute(senderType, (key, existing) -> existing == null ? commandPermission : Permission.anyOf(existing, commandPermission));
   }

   private static void updateSenderRequirements(final Set<Type> senderTypes, final Type senderType) {
      boolean add = true;
      Iterator<Type> iterator = senderTypes.iterator();

      while (iterator.hasNext()) {
         Type existingType = iterator.next();
         if (GenericTypeReflector.isSuperType(existingType, senderType)) {
            add = false;
            break;
         }

         if (GenericTypeReflector.isSuperType(senderType, existingType)) {
            iterator.remove();
            break;
         }
      }

      if (add) {
         senderTypes.add(senderType);
      }
   }

   private void checkAmbiguity(final @NonNull CommandNode<C> node) throws AmbiguousNodeException {
      if (!node.isLeaf()) {
         List<CommandNode<C>> childVariableArguments = node.children()
            .stream()
            .filter(n -> n.component() != null)
            .filter(n -> n.component().type() != CommandComponent.ComponentType.LITERAL)
            .collect(Collectors.toList());
         if (childVariableArguments.size() > 1) {
            CommandNode<C> child = childVariableArguments.get(0);
            throw new AmbiguousNodeException(node, child, node.children().stream().filter(n -> n.component() != null).collect(Collectors.toList()));
         }

         List<CommandNode<C>> childStaticArguments = node.children()
            .stream()
            .filter(n -> n.component() != null)
            .filter(n -> n.component().type() == CommandComponent.ComponentType.LITERAL)
            .collect(Collectors.toList());
         Set<String> checkedLiterals = new HashSet<>();

         for (CommandNode<C> child : childStaticArguments) {
            for (String nameOrAlias : child.component().aliases()) {
               if (!checkedLiterals.add(nameOrAlias)) {
                  throw new AmbiguousNodeException(node, child, node.children().stream().filter(n -> n.component() != null).collect(Collectors.toList()));
               }
            }
         }

         node.children().forEach(this::checkAmbiguity);
      }
   }

   @API(status = Status.INTERNAL)
   public @NonNull List<@NonNull CommandNode<C>> getLeavesRaw(final @NonNull CommandNode<C> node) {
      List<CommandNode<C>> leaves = new LinkedList<>();
      if (node.isLeaf()) {
         if (node.component() != null) {
            leaves.add(node);
         }
      } else {
         node.children().forEach(child -> leaves.addAll(this.getLeavesRaw((CommandNode<C>)child)));
      }

      return leaves;
   }

   private @NonNull List<@NonNull CommandNode<C>> getExecutorNodes(final @NonNull CommandNode<C> node) {
      List<CommandNode<C>> leaves = new LinkedList<>();
      if (node.command() != null) {
         leaves.add(node);
      }

      for (CommandNode<C> child : node.children()) {
         leaves.addAll(this.getExecutorNodes(child));
      }

      return leaves;
   }

   @API(status = Status.INTERNAL)
   public @NonNull List<@NonNull CommandNode<C>> getLeaves(final @NonNull CommandNode<C> node) {
      return this.getLeavesRaw(node).stream().filter(n -> n.component() != null).collect(Collectors.toList());
   }

   private @NonNull List<@NonNull CommandComponent<?>> getComponentChain(final @NonNull CommandNode<C> end) {
      return this.getChain(end).stream().map(CommandNode::component).filter(Objects::nonNull).collect(Collectors.toList());
   }

   private @NonNull List<@NonNull CommandNode<C>> getChain(final @Nullable CommandNode<C> end) {
      List<CommandNode<C>> chain = new LinkedList<>();

      for (CommandNode<C> tail = end; tail != null; tail = tail.parent()) {
         chain.add(tail);
      }

      Collections.reverse(chain);
      return chain;
   }

   void deleteRecursively(final CommandNode<C> node, final boolean root, final Consumer<Command<C>> commandConsumer) {
      for (CommandNode<C> child : new ArrayList<>(node.children())) {
         this.deleteRecursively(child, false, commandConsumer);
      }

      CommandComponent<C> component = node.component();
      Command<C> owner = component == null ? null : node.command();
      if (owner != null) {
         commandConsumer.accept(owner);
      }

      this.removeNode(node, root);
   }

   private void removeNode(final @NonNull CommandNode<C> node, final boolean root) {
      if (root) {
         this.internalTree.removeChild(node);
      } else {
         Objects.requireNonNull(node.parent(), "parent").removeChild(node);
      }
   }
}
