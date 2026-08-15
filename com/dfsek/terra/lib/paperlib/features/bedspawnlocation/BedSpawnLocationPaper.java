package com.dfsek.terra.lib.paperlib.features.bedspawnlocation;

import com.dfsek.terra.lib.paperlib.PaperLib;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BedSpawnLocationPaper implements BedSpawnLocation {
   @Override
   public CompletableFuture<Location> getBedSpawnLocationAsync(Player player, boolean isUrgent) {
      Location bedLocation = player.getPotentialBedLocation();
      return bedLocation != null && bedLocation.getWorld() != null
         ? PaperLib.getChunkAtAsync(bedLocation.getWorld(), bedLocation.getBlockX() >> 4, bedLocation.getBlockZ() >> 4, false, isUrgent)
            .thenCompose(chunk -> CompletableFuture.completedFuture(player.getBedSpawnLocation()))
         : CompletableFuture.completedFuture(null);
   }
}
