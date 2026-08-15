package com.dfsek.terra.lib.paperlib.features.asyncteleport;

import com.dfsek.terra.lib.paperlib.PaperLib;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class AsyncTeleportPaper implements AsyncTeleport {
   @Override
   public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, TeleportCause cause) {
      int x = location.getBlockX() >> 4;
      int z = location.getBlockZ() >> 4;
      return PaperLib.getChunkAtAsyncUrgently(location.getWorld(), x, z, true).thenApply(chunk -> entity.teleport(location, cause));
   }
}
