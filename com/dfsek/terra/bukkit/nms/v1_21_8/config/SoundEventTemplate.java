package com.dfsek.terra.bukkit.nms.v1_21_8.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundEventTemplate implements ObjectTemplate<SoundEvent> {
   @Value("id")
   @Default
   private ResourceLocation id = null;
   @Value("distance-to-travel")
   @Default
   private Float distanceToTravel = null;

   public SoundEvent get() {
      if (this.id == null) {
         return null;
      } else {
         return this.distanceToTravel == null ? SoundEvent.createVariableRangeEvent(this.id) : SoundEvent.createFixedRangeEvent(this.id, this.distanceToTravel);
      }
   }
}
