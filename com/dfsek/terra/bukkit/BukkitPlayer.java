package com.dfsek.terra.bukkit;

import com.dfsek.terra.api.entity.Player;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.bukkit.world.BukkitAdapter;
import com.dfsek.terra.lib.paperlib.PaperLib;
import org.bukkit.Location;

public class BukkitPlayer implements Player {
   private final org.bukkit.entity.Player delegate;

   public BukkitPlayer(org.bukkit.entity.Player delegate) {
      this.delegate = delegate;
   }

   public org.bukkit.entity.Player getHandle() {
      return this.delegate;
   }

   @Override
   public Vector3 position() {
      Location bukkit = this.delegate.getLocation();
      return Vector3.of(bukkit.getX(), bukkit.getY(), bukkit.getZ());
   }

   @Override
   public void position(Vector3 location) {
      PaperLib.teleportAsync(this.delegate, BukkitAdapter.adapt(location).toLocation(this.delegate.getWorld()));
   }

   @Override
   public void world(ServerWorld world) {
      Location newLoc = this.delegate.getLocation().clone();
      newLoc.setWorld(BukkitAdapter.adapt(world));
      PaperLib.teleportAsync(this.delegate, newLoc);
   }

   @Override
   public ServerWorld world() {
      return BukkitAdapter.adapt(this.delegate.getWorld());
   }
}
