package com.dfsek.terra.api.block.entity;

import com.dfsek.terra.api.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface MobSpawner extends BlockEntity {
   EntityType getSpawnedType();

   void setSpawnedType(@NotNull EntityType var1);

   int getDelay();

   void setDelay(int var1);

   int getMinSpawnDelay();

   void setMinSpawnDelay(int var1);

   int getMaxSpawnDelay();

   void setMaxSpawnDelay(int var1);

   int getSpawnCount();

   void setSpawnCount(int var1);

   int getMaxNearbyEntities();

   void setMaxNearbyEntities(int var1);

   int getRequiredPlayerRange();

   void setRequiredPlayerRange(int var1);

   int getSpawnRange();

   void setSpawnRange(int var1);
}
