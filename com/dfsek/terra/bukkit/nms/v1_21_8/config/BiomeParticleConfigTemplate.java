package com.dfsek.terra.bukkit.nms.v1_21_8.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.AmbientParticleSettings;

public class BiomeParticleConfigTemplate implements ObjectTemplate<AmbientParticleSettings> {
   @Value("particle")
   @Default
   private String particle = null;
   @Value("probability")
   @Default
   private Float probability = 0.1F;

   public AmbientParticleSettings get() {
      if (this.particle == null) {
         return null;
      }

      try {
         return new AmbientParticleSettings(
            ParticleArgument.readParticle(new StringReader(this.particle), Provider.create(Stream.of(BuiltInRegistries.PARTICLE_TYPE))), this.probability
         );
      } catch (CommandSyntaxException e) {
         throw new RuntimeException(e);
      }
   }
}
