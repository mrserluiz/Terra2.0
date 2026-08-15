package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BukkitUtils {
   private static final Logger logger = LoggerFactory.getLogger(BukkitUtils.class);

   public static boolean isLiquid(BlockData blockState) {
      Material material = blockState.getMaterial();
      return material == Material.WATER || material == Material.LAVA;
   }

   public static EntityType getEntityType(String id) {
      if (!id.contains(":")) {
         String newid = "minecraft:" + id.toLowerCase();
         logger.warn(
            "Translating "
               + id
               + " to "
               + newid
               + ". In 1.20.3 entity parsing was reworked. You are advised to perform this rename in your config backs as this translation will be removed in the next major version of Terra."
         );
      }

      if (!id.startsWith("minecraft:")) {
         throw new IllegalArgumentException("Invalid entity identifier " + id);
      }

      String entityID = id.toUpperCase(Locale.ROOT).substring(10);
      BukkitEntityType var10000 = new BukkitEntityType;

      var10000./* $VF: Unable to resugar constructor */<init>(switch (entityID) {
         case "END_CRYSTAL" -> org.bukkit.entity.EntityType.END_CRYSTAL;
         case "ENDER_CRYSTAL" -> throw new IllegalArgumentException("Invalid entity identifier " + id);
         default -> org.bukkit.entity.EntityType.valueOf(entityID);
      });
      return var10000;
   }
}
