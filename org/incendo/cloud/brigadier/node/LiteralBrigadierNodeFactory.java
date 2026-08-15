package org.incendo.cloud.brigadier.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.argument.ArgumentTypeFactory;
import org.incendo.cloud.brigadier.argument.BrigadierMapping;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionPredicate;
import org.incendo.cloud.brigadier.suggestion.BrigadierSuggestionFactory;
import org.incendo.cloud.brigadier.suggestion.CloudDelegatingSuggestionProvider;
import org.incendo.cloud.brigadier.suggestion.SuggestionsType;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.MappedArgumentParser;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.suggestion.SuggestionFactory;

@API(status = Status.STABLE, since = "2.0.0")
public final class LiteralBrigadierNodeFactory<C, S> implements BrigadierNodeFactory<C, S, LiteralCommandNode<S>> {
   private final CloudBrigadierManager<C, S> cloudBrigadierManager;
   private final CommandManager<C> commandManager;
   private final BrigadierSuggestionFactory<C, S> brigadierSuggestionFactory;

   public LiteralBrigadierNodeFactory(
      final @NonNull CloudBrigadierManager<C, S> cloudBrigadierManager,
      final @NonNull CommandManager<C> commandManager,
      final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory
   ) {
      this.cloudBrigadierManager = cloudBrigadierManager;
      this.commandManager = commandManager;
      this.brigadierSuggestionFactory = new BrigadierSuggestionFactory<>(cloudBrigadierManager, commandManager, suggestionFactory);
   }

   public @NonNull LiteralCommandNode<S> createNode(
      final @NonNull String label,
      final @NonNull CommandNode<C> cloudCommand,
      final @NonNull Command<S> executor,
      final @NonNull BrigadierPermissionChecker<C> permissionChecker
   ) {
      LiteralArgumentBuilder<S> literalArgumentBuilder = (LiteralArgumentBuilder<S>)LiteralArgumentBuilder.literal(label)
         .requires(this.requirement(cloudCommand, permissionChecker));
      this.updateExecutes(literalArgumentBuilder, cloudCommand, executor);
      LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();

      for (CommandNode<C> child : cloudCommand.children()) {
         constructedRoot.addChild(this.constructCommandNode(child, permissionChecker, executor).build());
      }

      return constructedRoot;
   }

   private @NonNull BrigadierPermissionPredicate<C, S> requirement(
      final @NonNull CommandNode<C> cloudCommand, final @NonNull BrigadierPermissionChecker<C> permissionChecker
   ) {
      return new BrigadierPermissionPredicate<>(this.cloudBrigadierManager.senderMapper(), permissionChecker, cloudCommand);
   }

   public @NonNull LiteralCommandNode<S> createNode(
      final @NonNull String label,
      final @NonNull Command<C> cloudCommand,
      final @NonNull Command<S> executor,
      final @NonNull BrigadierPermissionChecker<C> permissionChecker
   ) {
      CommandNode<C> node = this.commandManager.commandTree().getNamedNode(cloudCommand.rootComponent().name());
      Objects.requireNonNull(node, "node");
      return this.createNode(label, node, executor, permissionChecker);
   }

   public @NonNull LiteralCommandNode<S> createNode(final @NonNull String label, final @NonNull Command<C> cloudCommand, final @NonNull Command<S> executor) {
      return this.createNode(label, cloudCommand, executor, (sender, permission) -> this.commandManager.testPermission(sender, permission).allowed());
   }

   private @NonNull ArgumentBuilder<S, ?> constructCommandNode(
      final @NonNull CommandNode<C> root, final @NonNull BrigadierPermissionChecker<C> permissionChecker, final @NonNull Command<S> executor
   ) {
      if (root.component().parser() instanceof AggregateParser) {
         AggregateParser<C, ?> aggregateParser = (AggregateParser<C, ?>)root.component().parser();
         return this.constructAggregateNode(aggregateParser, root, permissionChecker, executor);
      }

      ArgumentBuilder<S, ?> argumentBuilder;
      if (root.component().type() == CommandComponent.ComponentType.LITERAL) {
         argumentBuilder = this.createLiteralArgumentBuilder(root.component(), root, permissionChecker);
      } else {
         argumentBuilder = this.createVariableArgumentBuilder(root.component(), root, permissionChecker);
      }

      this.updateExecutes(argumentBuilder, root, executor);

      for (CommandNode<C> node : root.children()) {
         argumentBuilder.then(this.constructCommandNode(node, permissionChecker, executor));
      }

      return argumentBuilder;
   }

   private @NonNull ArgumentBuilder<S, ?> createLiteralArgumentBuilder(
      final @NonNull CommandComponent<C> component, final @NonNull CommandNode<C> root, final @NonNull BrigadierPermissionChecker<C> permissionChecker
   ) {
      return LiteralArgumentBuilder.literal(component.name()).requires(this.requirement(root, permissionChecker));
   }

   private @NonNull ArgumentBuilder<S, ?> createVariableArgumentBuilder(
      final @NonNull CommandComponent<C> component, final @NonNull CommandNode<C> root, final @NonNull BrigadierPermissionChecker<C> permissionChecker
   ) {
      ArgumentMapping<S> argumentMapping = this.getArgument(component.valueType(), component.parser());
      SuggestionProvider<S> provider;
      if (argumentMapping.suggestionsType() == SuggestionsType.CLOUD_SUGGESTIONS) {
         provider = new CloudDelegatingSuggestionProvider<>(this.brigadierSuggestionFactory, root);
      } else {
         provider = argumentMapping.suggestionProvider();
      }

      return RequiredArgumentBuilder.argument(component.name(), argumentMapping.argumentType())
         .suggests(provider)
         .requires(this.requirement(root, permissionChecker));
   }

   private @NonNull ArgumentBuilder<S, ?> constructAggregateNode(
      final @NonNull AggregateParser<C, ?> aggregateParser,
      final @NonNull CommandNode<C> root,
      final @NonNull BrigadierPermissionChecker<C> permissionChecker,
      final @NonNull Command<S> executor
   ) {
      Iterator<CommandComponent<C>> components = aggregateParser.components().iterator();
      List<ArgumentBuilder<S, ?>> argumentBuilders = new ArrayList<>();

      while (components.hasNext()) {
         CommandComponent<C> component = components.next();
         ArgumentBuilder<S, ?> fragmentBuilder = this.createVariableArgumentBuilder(component, root, permissionChecker);
         if (this.cloudBrigadierManager.settings().get(BrigadierSetting.FORCE_EXECUTABLE)) {
            fragmentBuilder.executes(executor);
         }

         argumentBuilders.add(fragmentBuilder);
      }

      ArgumentBuilder<S, ?> tail = argumentBuilders.get(argumentBuilders.size() - 1);

      for (CommandNode<C> node : root.children()) {
         tail.then(this.constructCommandNode(node, permissionChecker, executor));
      }

      this.updateExecutes(tail, root, executor);

      for (int i = argumentBuilders.size() - 1; i > 0; i--) {
         argumentBuilders.get(i - 1).then(argumentBuilders.get(i));
      }

      return argumentBuilders.get(0);
   }

   private <K extends ArgumentParser<C, ?>> @NonNull ArgumentMapping<S> getArgument(final @NonNull TypeToken<?> valueType, final @NonNull K argumentParser) {
      if (argumentParser instanceof MappedArgumentParser) {
         return this.getArgument(valueType, (K)((MappedArgumentParser)argumentParser).baseParser());
      } else {
         BrigadierMapping<C, K, S> mapping = this.cloudBrigadierManager.mappings().mapping((Class<K>)argumentParser.getClass());
         if (mapping != null && mapping.mapper() != null) {
            SuggestionProvider<S> suggestionProvider = mapping.makeSuggestionProvider(argumentParser);
            return suggestionProvider == BrigadierMapping.delegateSuggestions()
               ? ImmutableArgumentMapping.<S>builder()
                  .argumentType((ArgumentType<?>)mapping.mapper().apply(argumentParser))
                  .suggestionsType(SuggestionsType.CLOUD_SUGGESTIONS)
                  .build()
               : ImmutableArgumentMapping.<S>builder()
                  .argumentType((ArgumentType<?>)mapping.mapper().apply(argumentParser))
                  .suggestionProvider(suggestionProvider)
                  .build();
         } else {
            return this.getDefaultMapping(valueType);
         }
      }
   }

   private @NonNull ArgumentMapping<S> getDefaultMapping(final @NonNull TypeToken<?> type) {
      ArgumentTypeFactory<?> argumentTypeSupplier = this.cloudBrigadierManager.defaultArgumentTypeFactories().get(GenericTypeReflector.erase(type.getType()));
      if (argumentTypeSupplier != null) {
         ArgumentType<?> argumentType = argumentTypeSupplier.create();
         if (argumentType != null) {
            return ImmutableArgumentMapping.<S>builder().argumentType(argumentType).build();
         }
      }

      return ImmutableArgumentMapping.<S>builder().argumentType(StringArgumentType.word()).suggestionsType(SuggestionsType.CLOUD_SUGGESTIONS).build();
   }

   private void updateExecutes(final @NonNull ArgumentBuilder<S, ?> builder, final @NonNull CommandNode<C> node, final @NonNull Command<S> executor) {
      if (this.cloudBrigadierManager.settings().get(BrigadierSetting.FORCE_EXECUTABLE)
         || node.isLeaf()
         || node.component().optional()
         || node.command() != null
         || node.children().stream().map(CommandNode::component).filter(Objects::nonNull).anyMatch(CommandComponent::optional)) {
         builder.executes(executor);
      }
   }
}
