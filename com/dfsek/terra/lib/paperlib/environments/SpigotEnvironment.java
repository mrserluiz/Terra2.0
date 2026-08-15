package com.dfsek.terra.lib.paperlib.environments;

public class SpigotEnvironment extends CraftBukkitEnvironment {
   @Override
   public String getName() {
      return "Spigot";
   }

   @Override
   public boolean isSpigot() {
      return true;
   }
}
