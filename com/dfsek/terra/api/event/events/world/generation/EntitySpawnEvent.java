package com.dfsek.terra.api.event.events.world.generation;

import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.entity.Entity;
import com.dfsek.terra.api.event.events.PackEvent;

public class EntitySpawnEvent implements PackEvent {
   private final ConfigPack pack;
   private final Entity entity;

   public EntitySpawnEvent(ConfigPack pack, Entity entity) {
      this.pack = pack;
      this.entity = entity;
   }

   @Override
   public ConfigPack getPack() {
      return this.pack;
   }

   public Entity getEntity() {
      return this.entity;
   }
}
