package com.dfsek.terra.bukkit.nms.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.nbt.TagParser;
import net.minecraft.core.particles.ParticleOptions;

import java.util.stream.Stream;


public class BiomeParticleConfigTemplate implements ObjectTemplate<AmbientParticle> {
    @Value("particle")
    @Default
    private String particle = null;

    @Value("probability")
    @Default
    private Float probability = 0.1f;

    @Override
    public AmbientParticle get() {
        if(particle == null) {
            return null;
        }

        try {
            return new AmbientParticle(parse(particle), probability);
        } catch(CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /** 26.3 renamed the block-state codec fields; translate legacy community SNBT only. */
    public static ParticleOptions parse(String source) throws CommandSyntaxException {
        int start=source.indexOf('{');
        if(start>=0) {
            var options=TagParser.parseCompoundFully(source.substring(start));
            options.getCompound("block_state").ifPresent(state -> {
                if(state.contains("Name") && !state.contains("id"))state.put("id",state.remove("Name"));
                if(state.contains("Properties") && !state.contains("properties"))state.put("properties",state.remove("Properties"));
            });
            source=source.substring(0,start)+options;
        }
        return ParticleArgument.readParticle(new StringReader(source),
            HolderLookup.Provider.create(Stream.of(BuiltInRegistries.PARTICLE_TYPE)));
    }
}
