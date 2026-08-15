package org.incendo.cloud;

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionRegistry;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionsProvider;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandContextFactory;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.context.StandardCommandContextFactory;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.execution.CommandExecutor;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.execution.postprocessor.AcceptingCommandPostprocessor;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.incendo.cloud.execution.preprocessor.AcceptingCommandPreprocessor;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;
import org.incendo.cloud.help.CommandPredicate;
import org.incendo.cloud.help.HelpHandler;
import org.incendo.cloud.help.HelpHandlerFactory;
import org.incendo.cloud.injection.ParameterInjectorRegistry;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.parser.StandardParserRegistry;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.permission.AndPermission;
import org.incendo.cloud.permission.OrPermission;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.permission.PredicatePermission;
import org.incendo.cloud.services.ServicePipeline;
import org.incendo.cloud.services.State;
import org.incendo.cloud.setting.Configurable;
import org.incendo.cloud.setting.ManagerSetting;
import org.incendo.cloud.state.RegistrationState;
import org.incendo.cloud.state.Stateful;
import org.incendo.cloud.suggestion.DelegatingSuggestionFactory;
import org.incendo.cloud.suggestion.FilteringSuggestionProcessor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionFactory;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.SuggestionProcessor;
import org.incendo.cloud.syntax.CommandSyntaxFormatter;
import org.incendo.cloud.syntax.StandardCommandSyntaxFormatter;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.type.tuple.Triplet;

@API(status = Status.STABLE)
public abstract class CommandManager<C> implements Stateful<RegistrationState>, CommandBuilderSource<C> {
   private final Configurable<ManagerSetting> settings = Configurable.enumConfigurable(ManagerSetting.class);
   private final ServicePipeline servicePipeline = ServicePipeline.builder().build();
   private final ParserRegistry<C> parserRegistry = new StandardParserRegistry<>();
   private final Collection<Command<C>> commands = new LinkedList<>();
   private final ParameterInjectorRegistry<C> parameterInjectorRegistry = new ParameterInjectorRegistry<>();
   private final CommandTree<C> commandTree;
   private final SuggestionFactory<C, ? extends Suggestion> suggestionFactory;
   private final Set<CloudCapability> capabilities = new HashSet<>();
   private final ExceptionController<C> exceptionController = new ExceptionController<>();
   private final CommandExecutor<C> commandExecutor;
   private CaptionFormatter<C, String> captionVariableReplacementHandler = CaptionFormatter.placeholderReplacing();
   private CommandSyntaxFormatter<C> commandSyntaxFormatter = new StandardCommandSyntaxFormatter<>(this);
   private SuggestionProcessor<C> suggestionProcessor = new FilteringSuggestionProcessor<>();
   private CommandRegistrationHandler<C> commandRegistrationHandler;
   private CaptionRegistry<C> captionRegistry;
   private HelpHandlerFactory<C> helpHandlerFactory = HelpHandlerFactory.standard(this);
   private SuggestionMapper<? extends Suggestion> mapper = SuggestionMapper.identity();
   private final AtomicReference<RegistrationState> state = new AtomicReference<>(RegistrationState.BEFORE_REGISTRATION);

   protected CommandManager(
      final @NonNull ExecutionCoordinator<C> executionCoordinator, final @NonNull CommandRegistrationHandler<C> commandRegistrationHandler
   ) {
      CommandContextFactory<C> commandContextFactory = new StandardCommandContextFactory<>(this);
      this.commandTree = CommandTree.newTree(this);
      this.commandRegistrationHandler = commandRegistrationHandler;
      this.suggestionFactory = new DelegatingSuggestionFactory<>(
         this, this.commandTree, commandContextFactory, executionCoordinator, suggestion -> this.mapper.map(suggestion)
      );
      this.commandExecutor = new StandardCommandExecutor<>(this, executionCoordinator, commandContextFactory);
      this.servicePipeline.registerServiceType(new TypeToken<CommandPreprocessor<C>>() {}, new AcceptingCommandPreprocessor<>());
      this.servicePipeline.registerServiceType(new TypeToken<CommandPostprocessor<C>>() {}, new AcceptingCommandPostprocessor<>());
      this.captionRegistry = CaptionRegistry.captionRegistry();
      this.captionRegistry.registerProvider(new StandardCaptionsProvider<>());
      this.parameterInjectorRegistry().registerInjector(CommandContext.class, (context, annotationAccessor) -> context);
   }

   @API(status = Status.STABLE)
   public @NonNull CommandExecutor<C> commandExecutor() {
      return this.commandExecutor;
   }

   @API(status = Status.STABLE)
   public @NonNull SuggestionFactory<C, ? extends Suggestion> suggestionFactory() {
      return this.suggestionFactory;
   }

   public @NonNull SuggestionMapper<? extends Suggestion> suggestionMapper() {
      return this.mapper;
   }

   public void appendSuggestionMapper(final @NonNull SuggestionMapper<? extends Suggestion> mapper) {
      this.suggestionMapper(this.suggestionMapper().then(mapper));
   }

   public void suggestionMapper(final @NonNull SuggestionMapper<? extends Suggestion> mapper) {
      this.mapper = Objects.requireNonNull(mapper, "mapper");
   }

   public @This @NonNull CommandManager<C> command(final @NonNull Command<? extends C> command) {
      if (!this.transitionIfPossible(RegistrationState.BEFORE_REGISTRATION, RegistrationState.REGISTERING) && !this.isCommandRegistrationAllowed()) {
         throw new IllegalStateException(
            "Unable to register commands because the manager is no longer in a registration state. Your platform may allow unsafe registrations by enabling the appropriate manager setting."
         );
      }

      this.commandTree.insertCommand((Command<C>)command);
      this.commands.add((Command<C>)command);
      return this;
   }

   @API(status = Status.STABLE)
   public @This @NonNull CommandManager<C> command(final @NonNull CommandFactory<C> commandFactory) {
      commandFactory.createCommands(this).forEach(this::command);
      return this;
   }

   public @NonNull CommandManager<C> command(final Command.@NonNull Builder<? extends C> command) {
      return this.command(command.manager(this).build());
   }

   @API(status = Status.STABLE)
   public @NonNull CaptionFormatter<C, String> captionFormatter() {
      return this.captionVariableReplacementHandler;
   }

   @API(status = Status.STABLE)
   public void captionFormatter(final @NonNull CaptionFormatter<C, String> captionFormatter) {
      this.captionVariableReplacementHandler = captionFormatter;
   }

   @API(status = Status.STABLE)
   public @NonNull CommandSyntaxFormatter<C> commandSyntaxFormatter() {
      return this.commandSyntaxFormatter;
   }

   @API(status = Status.STABLE)
   public void commandSyntaxFormatter(final @NonNull CommandSyntaxFormatter<C> commandSyntaxFormatter) {
      this.commandSyntaxFormatter = commandSyntaxFormatter;
   }

   public @NonNull CommandRegistrationHandler<C> commandRegistrationHandler() {
      return this.commandRegistrationHandler;
   }

   @API(status = Status.STABLE)
   protected final void commandRegistrationHandler(final @NonNull CommandRegistrationHandler<C> commandRegistrationHandler) {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      this.commandRegistrationHandler = commandRegistrationHandler;
   }

   @API(status = Status.STABLE)
   protected final void registerCapability(final @NonNull CloudCapability capability) {
      this.capabilities.add(capability);
   }

   @API(status = Status.STABLE)
   public boolean hasCapability(final @NonNull CloudCapability capability) {
      return this.capabilities.contains(capability);
   }

   @API(status = Status.STABLE)
   public @NonNull Collection<@NonNull CloudCapability> capabilities() {
      return Collections.unmodifiableSet(new HashSet<>(this.capabilities));
   }

   @API(status = Status.STABLE)
   public @NonNull PermissionResult testPermission(final @NonNull C sender, final @NonNull Permission permission) {
      if (permission instanceof PredicatePermission) {
         return ((PredicatePermission)permission).testPermission(sender);
      }

      if (permission instanceof OrPermission) {
         for (Permission innerPermission : permission.permissions()) {
            PermissionResult result = this.testPermission(sender, innerPermission);
            if (result.allowed()) {
               return result;
            }
         }

         return PermissionResult.denied(permission);
      } else if (permission instanceof AndPermission) {
         for (Permission innerPermission : permission.permissions()) {
            PermissionResult result = this.testPermission(sender, innerPermission);
            if (!result.allowed()) {
               return result;
            }
         }

         return PermissionResult.allowed(permission);
      } else {
         return PermissionResult.of(permission.isEmpty() || this.hasPermission(sender, permission.permissionString()), permission);
      }
   }

   @API(status = Status.STABLE)
   public final @NonNull CaptionRegistry<C> captionRegistry() {
      return this.captionRegistry;
   }

   @API(status = Status.STABLE)
   public final void captionRegistry(final @NonNull CaptionRegistry<C> captionRegistry) {
      this.captionRegistry = captionRegistry;
   }

   public abstract boolean hasPermission(@NonNull C sender, @NonNull String permission);

   @API(status = Status.EXPERIMENTAL)
   public void deleteRootCommand(final @NonNull String rootCommand) throws CloudCapability.CloudCapabilityMissingException {
      if (!this.hasCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION)) {
         throw new CloudCapability.CloudCapabilityMissingException(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
      }

      CommandNode<C> node = this.commandTree.getNamedNode(rootCommand);
      if (node != null && node.component() != null) {
         this.commandRegistrationHandler.unregisterRootCommand(node.component());
         this.commandTree.deleteRecursively(node, true, this.commands::remove);
      }
   }

   @API(status = Status.STABLE)
   public @NonNull Collection<@NonNull String> rootCommands() {
      return this.commandTree
         .rootNodes()
         .stream()
         .map(CommandNode::component)
         .filter(Objects::nonNull)
         .filter(component -> component.type() == CommandComponent.ComponentType.LITERAL)
         .map(CommandComponent::name)
         .collect(Collectors.toList());
   }

   @Override
   public final Command.@NonNull Builder<C> decorateBuilder(final Command.@NonNull Builder<C> builder) {
      return builder.manager(this);
   }

   @API(status = Status.STABLE)
   public <T> CommandComponent.@NonNull Builder<C, T> componentBuilder(final @NonNull Class<T> type, final @NonNull String name) {
      return CommandComponent.<C, T>ofType(type, name).commandManager(this);
   }

   public CommandFlag.@NonNull Builder<C, Void> flagBuilder(final @NonNull String name) {
      return CommandFlag.builder(name);
   }

   @API(status = Status.STABLE)
   public @NonNull CommandTree<C> commandTree() {
      return this.commandTree;
   }

   @Override
   public @NonNull CommandMeta createDefaultCommandMeta() {
      return CommandMeta.empty();
   }

   public void registerCommandPreProcessor(final @NonNull CommandPreprocessor<C> processor) {
      this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPreprocessor<C>>() {}, processor, Collections.emptyList());
   }

   public void registerCommandPostProcessor(final @NonNull CommandPostprocessor<C> processor) {
      this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPostprocessor<C>>() {}, processor, Collections.emptyList());
   }

   @API(status = Status.STABLE)
   public State preprocessContext(final @NonNull CommandContext<C> context, final @NonNull CommandInput commandInput) {
      this.servicePipeline.pump(CommandPreprocessingContext.of(context, commandInput)).through(new TypeToken<CommandPreprocessor<C>>() {}).complete();
      return context.<String>optional("__COMMAND_PRE_PROCESSED__").orElse("").isEmpty() ? State.REJECTED : State.ACCEPTED;
   }

   public State postprocessContext(final @NonNull CommandContext<C> context, final @NonNull Command<C> command) {
      this.servicePipeline.pump(CommandPostprocessingContext.of(context, command)).through(new TypeToken<CommandPostprocessor<C>>() {}).complete();
      return context.<String>optional("__COMMAND_POST_PROCESSED__").orElse("").isEmpty() ? State.REJECTED : State.ACCEPTED;
   }

   public @NonNull SuggestionProcessor<C> suggestionProcessor() {
      return this.suggestionProcessor;
   }

   public void suggestionProcessor(final @NonNull SuggestionProcessor<C> suggestionProcessor) {
      this.suggestionProcessor = suggestionProcessor;
   }

   @API(status = Status.STABLE)
   public @NonNull ParserRegistry<C> parserRegistry() {
      return this.parserRegistry;
   }

   public final @NonNull ParameterInjectorRegistry<C> parameterInjectorRegistry() {
      return this.parameterInjectorRegistry;
   }

   @API(status = Status.STABLE)
   public final @NonNull ExceptionController<C> exceptionController() {
      return this.exceptionController;
   }

   @API(status = Status.STABLE)
   public final @NonNull Collection<@NonNull Command<C>> commands() {
      return Collections.unmodifiableCollection(this.commands);
   }

   @API(status = Status.STABLE)
   public final @NonNull HelpHandler<C> createHelpHandler() {
      return this.helpHandlerFactory.createHelpHandler(cmd -> true);
   }

   @API(status = Status.STABLE)
   public final @NonNull HelpHandler<C> createHelpHandler(final @NonNull CommandPredicate<C> filter) {
      return this.helpHandlerFactory.createHelpHandler(filter);
   }

   @API(status = Status.STABLE)
   public final @NonNull HelpHandlerFactory<C> helpHandlerFactory() {
      return this.helpHandlerFactory;
   }

   @API(status = Status.STABLE)
   public final void helpHandlerFactory(final @NonNull HelpHandlerFactory<C> helpHandlerFactory) {
      this.helpHandlerFactory = helpHandlerFactory;
   }

   @API(status = Status.STABLE)
   public @NonNull Configurable<ManagerSetting> settings() {
      return this.settings;
   }

   public final @NonNull RegistrationState state() {
      return this.state.get();
   }

   public final boolean transitionIfPossible(final @NonNull RegistrationState in, final @NonNull RegistrationState out) {
      return this.state.compareAndSet(in, out) || this.state.get() == out;
   }

   @API(status = Status.STABLE)
   protected final void lockRegistration() {
      if (this.state() == RegistrationState.BEFORE_REGISTRATION) {
         this.transitionOrThrow(RegistrationState.BEFORE_REGISTRATION, RegistrationState.AFTER_REGISTRATION);
      } else {
         this.transitionOrThrow(RegistrationState.REGISTERING, RegistrationState.AFTER_REGISTRATION);
      }
   }

   @API(status = Status.STABLE)
   public boolean isCommandRegistrationAllowed() {
      return this.settings().get(ManagerSetting.ALLOW_UNSAFE_REGISTRATION) || this.state.get() != RegistrationState.AFTER_REGISTRATION;
   }

   protected void registerDefaultExceptionHandlers(
      final @NonNull Consumer<Triplet<CommandContext<C>, Caption, List<@NonNull CaptionVariable>>> messageSender,
      final @NonNull Consumer<Pair<String, Throwable>> logger
   ) {
      DefaultExceptionHandlers<C> defaultExceptionHandlers = new DefaultExceptionHandlers<>(messageSender, logger, this.exceptionController);
      defaultExceptionHandlers.register();
   }
}
