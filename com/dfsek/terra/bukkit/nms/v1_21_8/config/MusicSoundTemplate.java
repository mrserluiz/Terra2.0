package com.dfsek.terra.bukkit.nms.v1_21_8.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

public class MusicSoundTemplate implements ObjectTemplate<Music> {
   @Value("sound")
   @Default
   private SoundEvent sound = null;
   @Value("min-delay")
   @Default
   private Integer minDelay = null;
   @Value("max-delay")
   @Default
   private Integer maxDelay = null;
   @Value("replace-current-music")
   @Default
   private Boolean replaceCurrentMusic = null;

   public Music get() {
      return this.sound != null && this.minDelay != null && this.maxDelay != null && this.replaceCurrentMusic != null
         ? new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(this.sound), this.minDelay, this.maxDelay, this.replaceCurrentMusic)
         : null;
   }
}
