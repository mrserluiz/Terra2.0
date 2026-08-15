package org.incendo.cloud.paper.suggestion;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.paper.suggestion.tooltips.CompletionMapper;
import org.incendo.cloud.paper.suggestion.tooltips.CompletionMapperFactory;
import org.incendo.cloud.suggestion.SuggestionFactory;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.util.StringUtils;

class BrigadierAsyncCommandSuggestionListener<C> extends AsyncCommandSuggestionListener<C> {
   private final CompletionMapperFactory completionMapperFactory = CompletionMapperFactory.detectingRelocation();
   private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

   BrigadierAsyncCommandSuggestionListener(final @NonNull LegacyPaperCommandManager<C> paperCommandManager) {
      super(paperCommandManager);
      this.suggestionFactory = paperCommandManager.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);
   }

   @EventHandler
   @Override
   void onTabCompletion(final @NonNull AsyncTabCompleteEvent event) {
      super.onTabCompletion(event);
   }

   @Override
   protected Suggestions<C, ? extends TooltipSuggestion> querySuggestions(final @NonNull C commandSender, final @NonNull String input) {
      return this.suggestionFactory.suggestImmediately(commandSender, input);
   }

   @Override
   protected void setSuggestions(final @NonNull AsyncTabCompleteEvent event, final @NonNull C commandSender, final @NonNull String input) {
      CompletionMapper completionMapper = this.completionMapperFactory.createMapper();
      Suggestions<C, ? extends TooltipSuggestion> suggestions = this.querySuggestions(commandSender, input);
      event.completions(suggestions.list().stream().map(suggestion -> {
         String trim = StringUtils.trimBeforeLastSpace(suggestion.suggestion(), suggestions.commandInput());
         return trim == null ? null : suggestion.withSuggestion(trim);
      }).filter(Objects::nonNull).map(completionMapper::map).collect(Collectors.toList()));
   }
}
