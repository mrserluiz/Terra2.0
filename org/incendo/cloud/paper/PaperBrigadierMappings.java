package org.incendo.cloud.paper;

import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.paper.parser.KeyedWorldParser;

@API(status = Status.INTERNAL)
final class PaperBrigadierMappings {
   private PaperBrigadierMappings() {
   }

   static <C> void register(final @NonNull BukkitBrigadierMapper<C> mapper) {
      Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
      if (keyed != null && keyed.isAssignableFrom(World.class)) {
         mapper.mapSimpleNMS(new TypeToken<KeyedWorldParser<C>>() {}, "resource_location", true);
      }
   }
}
