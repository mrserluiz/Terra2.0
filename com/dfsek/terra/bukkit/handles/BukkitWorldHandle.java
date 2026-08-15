package com.dfsek.terra.bukkit.handles;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.api.handle.WorldHandle;
import com.dfsek.terra.bukkit.util.BukkitUtils;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BukkitWorldHandle implements WorldHandle {
   private static final Logger logger = LoggerFactory.getLogger(BukkitWorldHandle.class);
   private final BlockState air = BukkitBlockState.newInstance(Material.AIR.createBlockData());

   @NotNull
   @Override
   public synchronized BlockState createBlockState(@NotNull String data) {
      if (data.equals("minecraft:grass")) {
         data = "minecraft:short_grass";
         logger.warn(
            "Translating minecraft:grass to minecraft:short_grass. In 1.20.3 minecraft:grass was renamed to minecraft:short_grass. You are advised to perform this rename in your config backs as this translation will be removed in the next major version of Terra."
         );
      }

      BlockData bukkitData = Bukkit.createBlockData(data);
      return BukkitBlockState.newInstance(bukkitData);
   }

   @NotNull
   @Override
   public BlockState air() {
      return this.air;
   }

   @NotNull
   @Override
   public EntityType getEntity(@NotNull String id) {
      return BukkitUtils.getEntityType(id);
   }
}
