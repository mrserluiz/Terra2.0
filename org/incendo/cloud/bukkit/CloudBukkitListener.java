package org.incendo.cloud.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

final class CloudBukkitListener<C> implements Listener {
   private final BukkitCommandManager<C> bukkitCommandManager;

   CloudBukkitListener(final @NonNull BukkitCommandManager<C> bukkitCommandManager) {
      this.bukkitCommandManager = bukkitCommandManager;
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   void onPlayerLogin(final @NonNull PlayerSpawnLocationEvent event) {
      this.bukkitCommandManager.lockIfBrigadierCapable();
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   void onPluginDisable(final @NonNull PluginDisableEvent event) {
      if (event.getPlugin().equals(this.bukkitCommandManager.owningPlugin())) {
         this.bukkitCommandManager.rootCommands().forEach(this.bukkitCommandManager::deleteRootCommand);
      }
   }
}
