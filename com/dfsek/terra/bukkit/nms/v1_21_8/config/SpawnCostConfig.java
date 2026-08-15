package com.dfsek.terra.bukkit.nms.v1_21_8.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import net.minecraft.world.entity.EntityType;

public class SpawnCostConfig implements ObjectTemplate<SpawnCostConfig> {
   @Value("type")
   @Default
   private EntityType<?> type = null;
   @Value("mass")
   @Default
   private Double mass = null;
   @Value("gravity")
   @Default
   private Double gravity = null;

   public EntityType<?> getType() {
      return this.type;
   }

   public Double getMass() {
      return this.mass;
   }

   public Double getGravity() {
      return this.gravity;
   }

   public SpawnCostConfig get() {
      return this;
   }
}
