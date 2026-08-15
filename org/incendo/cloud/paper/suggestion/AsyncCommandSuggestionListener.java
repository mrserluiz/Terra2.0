package org.incendo.cloud.paper.suggestion;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitPluginRegistrationHandler;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.util.StringUtils;

class AsyncCommandSuggestionListener<C> implements SuggestionListener<C> {
   private final LegacyPaperCommandManager<C> paperCommandManager;

   AsyncCommandSuggestionListener(final @NonNull LegacyPaperCommandManager<C> paperCommandManager) {
      this.paperCommandManager = paperCommandManager;
   }

   @EventHandler
   void onTabCompletion(final @NonNull AsyncTabCompleteEvent event) {
      String strippedBuffer = event.getBuffer().startsWith("/") ? event.getBuffer().substring(1) : event.getBuffer();
      if (!strippedBuffer.trim().isEmpty()) {
         BukkitPluginRegistrationHandler<C> bukkitPluginRegistrationHandler = (BukkitPluginRegistrationHandler<C>)this.paperCommandManager
            .commandRegistrationHandler();
         String commandLabel = strippedBuffer.split(" ")[0];
         if (bukkitPluginRegistrationHandler.isRecognized(commandLabel)) {
            String input = event.getBuffer();
            if (input.charAt(0) == '/') {
               input = input.substring(1);
            }

            this.setSuggestions(
               event, this.paperCommandManager.senderMapper().map(event.getSender()), BukkitHelper.stripNamespace(this.paperCommandManager, input)
            );
            event.setHandled(true);
         }
      }
   }

   protected Suggestions<C, ?> querySuggestions(final @NonNull C commandSender, final @NonNull String input) {
      return this.paperCommandManager.suggestionFactory().suggestImmediately(commandSender, input);
   }

   protected void setSuggestions(final @NonNull AsyncTabCompleteEvent event, final @NonNull C commandSender, final @NonNull String input) {
      Suggestions<C, ?> suggestions = this.querySuggestions(commandSender, input);
      event.setCompletions(
         suggestions.list()
            .stream()
            .map(Suggestion::suggestion)
            .map(suggestion -> StringUtils.trimBeforeLastSpace(suggestion, suggestions.commandInput()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
      );
   }
}
