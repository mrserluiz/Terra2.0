package com.dfsek.terra.bukkit.hooks;

import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.registry.key.Keyed;
import java.util.Collection;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.generators.GeneratorPlugin;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;

public final class MultiverseGeneratorPluginHook implements GeneratorPlugin {
   private final Platform platform;

   public MultiverseGeneratorPluginHook(Platform platform) {
      this.platform = platform;
   }

   @NotNull
   public Collection<String> suggestIds(@Nullable String s) {
      return this.platform.getConfigRegistry().entries().stream().map(Keyed::getID).toList();
   }

   @Nullable
   public Collection<String> getExampleUsages() {
      return this.platform
         .getConfigRegistry()
         .entries()
         .stream()
         .map(Keyed::getID)
         .map(xva$0 -> "/mv create example_world NORMAL -g Terra:%s".formatted(xva$0))
         .limit(5L)
         .toList();
   }

   @Nullable
   public String getInfoLink() {
      return "https://terra.polydev.org/";
   }

   @NotNull
   public String getPluginName() {
      return "Terra";
   }

   public static void register(Platform platform) {
      MultiverseCoreApi.get().getGeneratorProvider().registerGeneratorPlugin(new MultiverseGeneratorPluginHook(platform));
   }
}
