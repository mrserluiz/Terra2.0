package com.dfsek.terra.bukkit.nms;

import com.dfsek.seismic.type.sampler.Sampler;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/** Paper 26.3 compiles density graphs into seeded samplers. No synthetic old noise-settings JSON. */
public final class NativeOldBlendedNoise implements Sampler {
    private final LoadingCache<Long,DensitySampler> seeded;
    public NativeOldBlendedNoise(net.minecraft.core.HolderLookup.Provider registries,
            double xzScale,double yScale,double xzFactor,double yFactor,double smear) {
        var operator=new BlendedNoise(xzScale,yScale,xzFactor,yFactor,smear);
        var zero=DensityFunctions.constant(0);
        var router=new NoiseRouter(zero,zero,zero,zero,zero,zero,zero,operator);
        var noises=registries.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE);
        seeded=Caffeine.newBuilder().maximumSize(64).build(seed->RandomState.create(noises,seed,false,
            net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),-64,router).getSampler(operator));
    }
    @Override public double getSample(long seed,double x,double z){throw new UnsupportedOperationException("3D density only");}
    @Override public double getSample(long seed,double x,double y,double z){
        return seeded.get(seed).sampleValue(SamplerContext.EMPTY_UNCACHED,(int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z));
    }
}
