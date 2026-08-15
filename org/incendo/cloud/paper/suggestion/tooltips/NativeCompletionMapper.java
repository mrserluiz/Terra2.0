package org.incendo.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import com.mojang.brigadier.Message;
import io.papermc.paper.brigadier.PaperBrigadier;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.jetbrains.annotations.NotNull;

final class NativeCompletionMapper implements CompletionMapper {
   @Override
   public @NonNull Completion map(final @NonNull TooltipSuggestion suggestion) {
      return !CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.MessageComponentSerializer")
         ? mapLegacy(suggestion)
         : Completion.completion(suggestion.suggestion(), MessageComponentSerializer.message().deserializeOrNull(suggestion.tooltip()));
   }

   private static @NonNull Completion mapLegacy(@NotNull final TooltipSuggestion suggestion) {
      Message tooltip = suggestion.tooltip();
      return tooltip == null
         ? Completion.completion(suggestion.suggestion())
         : Completion.completion(suggestion.suggestion(), PaperBrigadier.componentFromMessage(tooltip));
   }
}
