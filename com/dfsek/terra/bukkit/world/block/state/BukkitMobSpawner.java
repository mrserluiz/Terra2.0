package com.dfsek.terra.bukkit.world.block.state;

import com.dfsek.terra.api.block.entity.MobSpawner;
import com.dfsek.terra.api.block.entity.SerialState;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.bukkit.util.BukkitUtils;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;
import org.bukkit.block.CreatureSpawner;
import org.jetbrains.annotations.NotNull;

public class BukkitMobSpawner extends BukkitBlockEntity implements MobSpawner {
   protected BukkitMobSpawner(CreatureSpawner block) {
      super(block);
   }

   @Override
   public EntityType getSpawnedType() {
      return new BukkitEntityType(((CreatureSpawner)this.getHandle()).getSpawnedType());
   }

   @Override
   public void setSpawnedType(@NotNull EntityType creatureType) {
      ((CreatureSpawner)this.getHandle()).setSpawnedType(((BukkitEntityType)creatureType).getHandle());
   }

   @Override
   public int getDelay() {
      return ((CreatureSpawner)this.getHandle()).getDelay();
   }

   @Override
   public void setDelay(int delay) {
      ((CreatureSpawner)this.getHandle()).setDelay(delay);
   }

   @Override
   public int getMinSpawnDelay() {
      return ((CreatureSpawner)this.getHandle()).getMinSpawnDelay();
   }

   @Override
   public void setMinSpawnDelay(int delay) {
      ((CreatureSpawner)this.getHandle()).setMinSpawnDelay(delay);
   }

   @Override
   public int getMaxSpawnDelay() {
      return ((CreatureSpawner)this.getHandle()).getMaxSpawnDelay();
   }

   @Override
   public void setMaxSpawnDelay(int delay) {
      ((CreatureSpawner)this.getHandle()).setMaxSpawnDelay(delay);
   }

   @Override
   public int getSpawnCount() {
      return ((CreatureSpawner)this.getHandle()).getSpawnCount();
   }

   @Override
   public void setSpawnCount(int spawnCount) {
      ((CreatureSpawner)this.getHandle()).setSpawnCount(spawnCount);
   }

   @Override
   public int getMaxNearbyEntities() {
      return ((CreatureSpawner)this.getHandle()).getMaxNearbyEntities();
   }

   @Override
   public void setMaxNearbyEntities(int maxNearbyEntities) {
      ((CreatureSpawner)this.getHandle()).setMaxNearbyEntities(maxNearbyEntities);
   }

   @Override
   public int getRequiredPlayerRange() {
      return ((CreatureSpawner)this.getHandle()).getRequiredPlayerRange();
   }

   @Override
   public void setRequiredPlayerRange(int requiredPlayerRange) {
      ((CreatureSpawner)this.getHandle()).setRequiredPlayerRange(requiredPlayerRange);
   }

   @Override
   public int getSpawnRange() {
      return ((CreatureSpawner)this.getHandle()).getSpawnRange();
   }

   @Override
   public void setSpawnRange(int spawnRange) {
      ((CreatureSpawner)this.getHandle()).setSpawnRange(spawnRange);
   }

   @Override
   public void applyState(String state) {
      SerialState.parse(state).forEach((k, v) -> {
         switch (k) {
            case "type":
               this.setSpawnedType(BukkitUtils.getEntityType(v));
               break;
            case "delay":
               this.setDelay(Integer.parseInt(v));
               break;
            case "min_delay":
               this.setMinSpawnDelay(Integer.parseInt(v));
               break;
            case "max_delay":
               this.setMaxSpawnDelay(Integer.parseInt(v));
               break;
            case "spawn_count":
               this.setSpawnCount(Integer.parseInt(v));
               break;
            case "spawn_range":
               this.setSpawnRange(Integer.parseInt(v));
               break;
            case "max_nearby":
               this.setMaxNearbyEntities(Integer.parseInt(v));
               break;
            case "required_player_range":
               this.setRequiredPlayerRange(Integer.parseInt(v));
               break;
            default:
               throw new IllegalArgumentException("Invalid property: " + k);
         }
      });
   }
}
