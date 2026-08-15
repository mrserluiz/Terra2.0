package org.incendo.cloud.bukkit.internal;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.BukkitCommandMeta;
import org.incendo.cloud.bukkit.PluginHolder;
import org.incendo.cloud.description.CommandDescription;

@API(status = Status.INTERNAL)
public final class BukkitHelper {
   private BukkitHelper() {
   }

   public static @NonNull String description(final @NonNull Command<?> command) {
      Optional<String> bukkitDescription = command.commandMeta().optional(BukkitCommandMeta.BUKKIT_DESCRIPTION);
      if (bukkitDescription.isPresent()) {
         return bukkitDescription.get();
      }

      CommandDescription description = command.commandDescription();
      return !description.isEmpty() ? description.description().textDescription() : command.rootComponent().description().textDescription();
   }

   public static @NonNull String namespacedLabel(final @NonNull PluginHolder manager, final @NonNull String label) {
      return namespacedLabel(manager.owningPlugin().getName(), label);
   }

   public static @NonNull String namespacedLabel(final @NonNull String pluginName, final @NonNull String label) {
      return (pluginName + ':' + label).toLowerCase(Locale.ROOT);
   }

   public static @NonNull String stripNamespace(final @NonNull PluginHolder manager, final @NonNull String command) {
      return stripNamespace(manager.owningPlugin().getName(), command);
   }

   public static @NonNull String stripNamespace(final @NonNull String pluginName, final @NonNull String command) {
      String[] split = command.split(" ");
      if (!split[0].contains(":")) {
         return command;
      } else {
         String token = split[0];
         String[] splitToken = token.split(":");
         if (namespacedLabel(pluginName, splitToken[1]).equals(token)) {
            split[0] = splitToken[1];
            return String.join(" ", split);
         } else {
            return command;
         }
      }
   }

   public static @NonNull Executor mainThreadExecutor(final @NonNull PluginHolder pluginHolder) {
      Plugin plugin = pluginHolder.owningPlugin();
      Server server = plugin.getServer();
      return task -> {
         if (server.isPrimaryThread()) {
            task.run();
         } else {
            server.getScheduler().runTask(plugin, task);
         }
      };
   }

   public static void ensurePluginEnabledOrEnabling(final @NonNull Plugin plugin) {
      Plugin fromManager = Bukkit.getServer().getPluginManager().getPlugin(plugin.getName());
      if (!plugin.equals(fromManager) || !plugin.isEnabled()) {
         throw new IllegalStateException(
            "The plugin '"
               + plugin
               + "' is not (yet?) valid per the PluginManager. Try calling this method from onEnable rather than in the plugin constructor or onLoad."
         );
      }
   }
}
