package com.dfsek.terra.bukkit.listeners;

import com.dfsek.terra.api.Platform;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpigotListener implements Listener {
   private static final Logger logger = LoggerFactory.getLogger(SpigotListener.class);
   private final Platform platform;

   public SpigotListener(Platform platform) {
      this.platform = platform;
   }

   @EventHandler(priority = EventPriority.NORMAL)
   public void onEnderEye(EntitySpawnEvent e) {
   }

   @EventHandler
   public void onCartographerChange(VillagerAcquireTradeEvent e) {
      if (e.getEntity() instanceof Villager) {
         if (((Villager)e.getEntity()).getProfession() == Profession.CARTOGRAPHER) {
            logger.error(
               ".------------------------------------------------------------------------.\n|     Prevented server crash by stopping Cartographer villager from      |\n|  spawning. Please upgrade to Paper, which has a StructureLocateEvent   |\n|   that fixes this issue at the source, and doesn't require us to do    |\n|                           stupid band-aids.                            |\n|------------------------------------------------------------------------|\n"
                  .strip()
            );
            e.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onCartographerLevel(VillagerCareerChangeEvent e) {
      if (e.getProfession() == Profession.CARTOGRAPHER) {
         logger.error(
            ".------------------------------------------------------------------------.\n| Prevented server crash by stopping Cartographer villager from leveling |\n|   up. Please upgrade to Paper, which has a StructureLocateEvent that   |\n|  fixes this issue at the source, and doesn't require us to do stupid   |\n|                               band-aids.                               |\n|------------------------------------------------------------------------|\n"
               .strip()
         );
         e.getEntity().setProfession(Profession.NITWIT);
         e.setCancelled(true);
      }
   }
}
