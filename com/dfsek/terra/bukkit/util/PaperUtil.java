package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.lib.paperlib.PaperLib;
import java.util.concurrent.TimeUnit;

public final class PaperUtil {
   public static void checkPaper(TerraBukkitPlugin plugin) {
      plugin.getAsyncScheduler().runDelayed(plugin, task -> {
         if (!PaperLib.isPaper()) {
            PaperLib.suggestPaper(plugin);
         }
      }, 100L, TimeUnit.SECONDS);
   }
}
