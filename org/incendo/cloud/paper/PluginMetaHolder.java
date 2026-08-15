package org.incendo.cloud.paper;

import io.papermc.paper.plugin.configuration.PluginMeta;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.bukkit.PluginHolder;

public interface PluginMetaHolder extends PluginHolder {
   PluginMeta owningPluginMeta();

   @Override
   default Plugin owningPlugin() {
      return Objects.requireNonNull(
         Bukkit.getPluginManager().getPlugin(this.owningPluginMeta().getName()), () -> this.owningPluginMeta().getName() + " Plugin instance"
      );
   }

   @API(status = Status.INTERNAL)
   static PluginMetaHolder fromPluginHolder(final PluginHolder pluginHolder) {
      return new PluginMetaHolder() {
         @Override
         public PluginMeta owningPluginMeta() {
            return pluginHolder.owningPlugin().getPluginMeta();
         }

         @Override
         public Plugin owningPlugin() {
            return pluginHolder.owningPlugin();
         }
      };
   }
}
