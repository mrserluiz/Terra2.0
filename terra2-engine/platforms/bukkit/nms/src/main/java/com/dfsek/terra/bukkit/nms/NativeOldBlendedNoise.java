package com.dfsek.terra.bukkit.nms;

import com.dfsek.seismic.type.sampler.Sampler;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/** Uses the server codec and RandomState, including its terrain random fork and seed algorithm. */
public final class NativeOldBlendedNoise implements Sampler {
    private final LoadingCache<Long, DensityFunction> seeded;
    public NativeOldBlendedNoise(net.minecraft.core.HolderLookup.Provider registries,
            double xzScale, double yScale, double xzFactor, double yFactor, double smear) {
        JsonObject json = com.google.gson.JsonParser.parseString(SETTINGS).getAsJsonObject();
        JsonObject operator = new JsonObject();
        operator.addProperty("type", "minecraft:old_blended_noise");
        operator.addProperty("xz_scale", xzScale); operator.addProperty("y_scale", yScale);
        operator.addProperty("xz_factor", xzFactor); operator.addProperty("y_factor", yFactor);
        operator.addProperty("smear_scale_multiplier", smear);
        json.getAsJsonObject("noise_router").add("final_density", operator);
        var settings = NoiseGeneratorSettings.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries), json)
            .getOrThrow(message -> new IllegalArgumentException("Invalid native density: " + message));
        HolderGetter<NormalNoise.NoiseParameters> noises = registries.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE);
        seeded = Caffeine.newBuilder().maximumSize(64).build(seed -> RandomState.create(settings, noises, seed).router().finalDensity());
    }
    @Override public double getSample(long seed, double x, double z) { throw new UnsupportedOperationException("3D density only"); }
    @Override public double getSample(long seed, double x, double y, double z) {
        return seeded.get(seed).compute(new DensityFunction.SinglePointContext((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z)));
    }
    // Scaffolding only: final_density is replaced with the requested operator before decoding.
    private static final String SETTINGS = """
        {"aquifers_enabled":false,"default_block":{"Name":"minecraft:stone"},"default_fluid":{"Name":"minecraft:water"},
         "disable_mob_generation":true,"legacy_random_source":false,
         "noise":{"height":128,"min_y":0,"size_horizontal":2,"size_vertical":1},
         "noise_router":{"barrier":0,"continents":0,"depth":0,"erosion":0,"final_density":0,
          "fluid_level_floodedness":0,"fluid_level_spread":0,"initial_density_without_jaggedness":0,
          "lava":0,"ridges":0,"temperature":0,"vegetation":0,"vein_gap":0,"vein_ridged":0,"vein_toggle":0},
         "ore_veins_enabled":false,"sea_level":-64,"spawn_target":[],
         "surface_rule":{"type":"minecraft:block","result_state":{"Name":"minecraft:stone"}}}
        """;
}
