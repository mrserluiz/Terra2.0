package com.dfsek.terra.bukkit.nms;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AetherDensityParityTest {
    @Test void matchesOriginalAetherDensityAtEveryCellCornerAcrossSeedsAndGradients() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var registries = VanillaRegistries.createLookup();
        JsonObject original;
        try(var reader = new java.io.InputStreamReader(getClass().getResourceAsStream("/aether-skylands-1.21.1.json"))) {
            original = JsonParser.parseReader(reader).getAsJsonObject();
        }
        // Only unavailable mod blocks are substituted. Density graph and random source are unchanged.
        original.add("default_block", JsonParser.parseString("{\"Name\":\"minecraft:stone\"}"));
        original.add("surface_rule", JsonParser.parseString("{\"type\":\"minecraft:block\",\"result_state\":{\"Name\":\"minecraft:stone\"}}"));
        var router = original.getAsJsonObject("noise_router");
        // Resolve the one named mod function from its original JSON definition.
        String density = router.get("final_density").toString().replace("\"aether:base_3d_noise_aether\"",
            "{\"type\":\"minecraft:old_blended_noise\",\"xz_scale\":0.25,\"y_scale\":0.25,\"xz_factor\":80,\"y_factor\":160,\"smear_scale_multiplier\":8}");
        JsonObject squeeze = JsonParser.parseString(density).getAsJsonObject();
        JsonElement unsqueezed = squeeze.getAsJsonObject("argument").get("argument");
        router.add("final_density", unsqueezed); // blend_density is identity on SinglePointContext, new chunks only.
        router.addProperty("temperature",0); router.addProperty("vegetation",0);
        var expectedSettings = NoiseGeneratorSettings.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE,registries), original).getOrThrow();
        var actual = new NativeOldBlendedNoise(registries,0.25,0.25,80,160,8);
        for(long seed : new long[]{0,42,-917234567890L}) {
            var expected = RandomState.create(expectedSettings,registries.lookupOrThrow(Registries.NOISE),seed).router().finalDensity();
            for(int x : new int[]{-1024,-8,0,8,760}) for(int z : new int[]{-776,-8,0,8,1024})
                for(int y=0;y<=132;y+=4) {
                    double lower=Math.clamp((y-8)/32.0,0,1), upper=Math.clamp((128-y)/72.0,0,1);
                    double value=-0.15+lower*(-0.1+upper*(0.07+actual.getSample(seed,x,y,z)));
                    assertEquals(expected.compute(new DensityFunction.SinglePointContext(x,y,z)),value,1e-12,
                        "seed="+seed+" xyz="+x+","+y+","+z);
                }
        }
    }
}
