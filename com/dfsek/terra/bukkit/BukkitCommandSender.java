package com.dfsek.terra.bukkit;

import com.dfsek.terra.api.command.CommandSender;
import com.dfsek.terra.api.entity.Entity;
import com.dfsek.terra.api.entity.Player;
import com.dfsek.terra.bukkit.world.BukkitAdapter;
import java.util.Optional;
import org.bukkit.ChatColor;

public class BukkitCommandSender implements CommandSender {
   private final org.bukkit.command.CommandSender delegate;

   public BukkitCommandSender(org.bukkit.command.CommandSender delegate) {
      this.delegate = delegate;
   }

   @Override
   public void sendMessage(String message) {
      this.delegate.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
   }

   @Override
   public Optional<Entity> getEntity() {
      return this.delegate instanceof org.bukkit.entity.Entity entity ? Optional.of(BukkitAdapter.adapt(entity)) : Optional.empty();
   }

   @Override
   public Optional<Player> getPlayer() {
      return this.delegate instanceof org.bukkit.entity.Player player ? Optional.of(BukkitAdapter.adapt(player)) : Optional.empty();
   }

   public org.bukkit.command.CommandSender getHandle() {
      return this.delegate;
   }
}
