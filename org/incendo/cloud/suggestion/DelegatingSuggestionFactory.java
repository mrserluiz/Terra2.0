package org.incendo.cloud.suggestion;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandContextFactory;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.services.State;
import org.incendo.cloud.setting.ManagerSetting;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class DelegatingSuggestionFactory<C, S extends Suggestion> implements SuggestionFactory<C, S> {
   private final List<S> singleEmptySuggestion;
   private final CommandManager<C> commandManager;
   private final CommandTree<C> commandTree;
   private final CommandContextFactory<C> contextFactory;
   private final ExecutionCoordinator<C> executionCoordinator;
   private final SuggestionMapper<S> mapper;

   public DelegatingSuggestionFactory(
      final @NonNull CommandManager<C> commandManager,
      final @NonNull CommandTree<C> commandTree,
      final @NonNull CommandContextFactory<C> contextFactory,
      final @NonNull ExecutionCoordinator<C> executionCoordinator,
      final @NonNull SuggestionMapper<S> mapper
   ) {
      this.commandManager = commandManager;
      this.commandTree = commandTree;
      this.contextFactory = contextFactory;
      this.executionCoordinator = executionCoordinator;
      this.mapper = mapper;
      this.singleEmptySuggestion = Collections.singletonList(mapper.map(Suggestion.suggestion("")));
   }

   @Override
   public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(final @NonNull CommandContext<C> context, final @NonNull String input) {
      return this.suggestFromTree(context, input);
   }

   @Override
   public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(final @NonNull C sender, final @NonNull String input) {
      return this.suggest(this.contextFactory.create(true, sender), input);
   }

   @Override
   public <S2 extends Suggestion> @NonNull SuggestionFactory<C, S2> mapped(final @NonNull SuggestionMapper<S2> mapper) {
      return new DelegatingSuggestionFactory<>(this.commandManager, this.commandTree, this.contextFactory, this.executionCoordinator, this.mapper.then(mapper));
   }

   private CompletableFuture<Suggestions<C, S>> suggestFromTree(final CommandContext<C> context, final String input) {
      CommandInput commandInput = CommandInput.of(input);
      context.store("__raw_input__", commandInput.copy());
      if (this.commandManager.preprocessContext(context, commandInput) != State.ACCEPTED) {
         return this.commandManager.settings().get(ManagerSetting.FORCE_SUGGESTION)
            ? CompletableFuture.completedFuture(Suggestions.create(context, this.singleEmptySuggestion, commandInput))
            : CompletableFuture.completedFuture(Suggestions.create(context, Collections.emptyList(), commandInput));
      } else {
         return this.executionCoordinator
            .coordinateSuggestions(this.commandTree, context, commandInput, this.mapper)
            .thenApply(
               suggestions -> this.commandManager.settings().get(ManagerSetting.FORCE_SUGGESTION) && suggestions.list().isEmpty()
                  ? Suggestions.create(suggestions.commandContext(), this.singleEmptySuggestion, commandInput)
                  : suggestions
            );
      }
   }
}
