package com.dfsek.terra.bukkit.nms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate.Sampler;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.bukkit.world.BukkitPlatformBiome;


public class NMSBiomeProvider extends BiomeSource {
    private final BiomeProvider delegate;
    private final long seed;
    private final org.terra2.adapter.terra.CoreTerraGenerator core;
    private final com.dfsek.terra.api.world.info.WorldProperties world;
    private final Registry<Biome> biomeRegistry = RegistryFetcher.biomeRegistry();

    public NMSBiomeProvider(BiomeProvider delegate, long seed) {
        this(delegate, seed, null, null);
    }

    public NMSBiomeProvider(BiomeProvider delegate, long seed, org.terra2.adapter.terra.CoreTerraGenerator core,
                            com.dfsek.terra.api.world.info.WorldProperties world) {
        super();
        this.core = core;
        this.world = world;
        this.delegate = delegate;
        this.seed = seed;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return delegate.stream()
            .map(biome -> RegistryFetcher.biomeRegistry()
                .getOrThrow(((BukkitPlatformBiome) biome.getPlatformBiome()).getContext()
                    .get(NMSBiomeInfo.class)
                    .biomeKey()));
    }

    @Override
    protected @NotNull MapCodec<? extends BiomeSource> codec() {
        return MapCodec.assumeMapUnsafe(BiomeSource.CODEC);
        //        return MapCodec.unit(null);
        //        BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchMap(this::codec, Function.identity());
        //        BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
        //        return BiomeSource.CODEC;
    }

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int x, int y, int z, @NotNull Sampler sampler) {
        var biome = core == null ? delegate.getBiome(x << 2, y << 2, z << 2, seed)
            : core.getBiome(world, x << 2, y << 2, z << 2);
        return biomeRegistry.getOrThrow(((BukkitPlatformBiome) biome.getPlatformBiome()).getContext()
            .get(NMSBiomeInfo.class)
            .biomeKey());
    }
}
