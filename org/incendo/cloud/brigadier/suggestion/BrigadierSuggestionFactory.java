package org.incendo.cloud.brigadier.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.suggestion.SuggestionFactory;
import org.incendo.cloud.type.tuple.Pair;

@API(status = Status.INTERNAL, since = "2.0.0")
public final class BrigadierSuggestionFactory<C, S> {
   private final CloudBrigadierManager<C, S> cloudBrigadierManager;
   private final CommandManager<C> commandManager;
   private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

   public BrigadierSuggestionFactory(
      final @NonNull CloudBrigadierManager<C, S> cloudBrigadierManager,
      final @NonNull CommandManager<C> commandManager,
      final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory
   ) {
      this.cloudBrigadierManager = cloudBrigadierManager;
      this.commandManager = commandManager;
      this.suggestionFactory = suggestionFactory;
   }

   public @NonNull CompletableFuture<@NonNull Suggestions> buildSuggestions(
      final @NonNull CommandContext<S> senderContext, final @Nullable CommandNode<C> parentNode, final @NonNull SuggestionsBuilder builder
   ) {
      C cloudSender = this.cloudBrigadierManager.senderMapper().map((S)senderContext.getSource());
      org.incendo.cloud.context.CommandContext<C> commandContext = new org.incendo.cloud.context.CommandContext<>(true, cloudSender, this.commandManager);
      commandContext.store("_cloud_brigadier_native_sender", senderContext.getSource());
      String command = builder.getInput()
         .substring(((StringRange)((Pair)CloudBrigadierCommand.parsedNodes(senderContext.getLastChild()).get(0)).second()).getStart());
      String leading = command.split(" ")[0];
      if (leading.contains(":")) {
         command = command.substring(leading.split(":")[0].length() + 1);
      }

      return this.suggestionFactory
         .suggest(commandContext.sender(), command)
         .thenApply(
            suggestionsResult -> {
               List<TooltipSuggestion> suggestions = new ArrayList<>(suggestionsResult.list());
               if (parentNode != null) {
                  Set<String> siblingLiterals = parentNode.children()
                     .stream()
                     .map(CommandNode::component)
                     .filter(Objects::nonNull)
                     .filter(c -> c.type() == CommandComponent.ComponentType.LITERAL)
                     .flatMap(commandComponent -> commandComponent.aliases().stream())
                     .collect(Collectors.toSet());
                  suggestions.removeIf(suggestionx -> siblingLiterals.contains(suggestionx.suggestion()));
               }

               int trimmed = builder.getInput().length() - suggestionsResult.commandInput().length();
               int rawOffset = suggestionsResult.commandInput().cursor();
               SuggestionsBuilder suggestionsBuilder = builder.createOffset(rawOffset + trimmed);

               for (TooltipSuggestion suggestion : suggestions) {
                  try {
                     suggestionsBuilder.suggest(Integer.parseInt(suggestion.suggestion()), suggestion.tooltip());
                  } catch (NumberFormatException e) {
                     suggestionsBuilder.suggest(suggestion.suggestion(), suggestion.tooltip());
                  }
               }

               return suggestionsBuilder.build();
            }
         );
   }
}
