package com.dfsek.terra.lib.paperlib.features.asyncteleport;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class AsyncTeleportSync implements AsyncTeleport {
   @Override
   public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, TeleportCause cause) {
      return CompletableFuture.completedFuture(entity.teleport(location, cause));
   }
}
