package org.incendo.cloud.paper.suggestion;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

@API(status = Status.INTERNAL, since = "2.0.0")
public interface SuggestionListenerFactory<C> {
   static <C> @NonNull SuggestionListenerFactory<C> create(final @NonNull LegacyPaperCommandManager<C> commandManager) {
      return new SuggestionListenerFactory.SuggestionListenerFactoryImpl<>(commandManager);
   }

   @NonNull SuggestionListener<C> createListener();

   final class SuggestionListenerFactoryImpl<C> implements SuggestionListenerFactory<C> {
      private final LegacyPaperCommandManager<C> commandManager;

      private SuggestionListenerFactoryImpl(final @NonNull LegacyPaperCommandManager<C> commandManager) {
         this.commandManager = commandManager;
      }

      @Override
      public SuggestionListener<C> createListener() {
         Class<?> completionCls = CraftBukkitReflection.findClass("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent$Completion");
         return completionCls != null
            ? new BrigadierAsyncCommandSuggestionListener<>(this.commandManager)
            : new AsyncCommandSuggestionListener<>(this.commandManager);
      }
   }
}
