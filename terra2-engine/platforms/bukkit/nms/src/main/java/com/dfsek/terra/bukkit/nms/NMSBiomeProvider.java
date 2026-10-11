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
    private final com.dfsek.terra.bukkit.compatibility.BiomeCompatibility compatibility;
    private final java.util.UUID worldId;
    private final String packId;
    private final long seed;
    private final org.terra2.adapter.terra.CoreTerraGenerator core;
    private final com.dfsek.terra.api.world.info.WorldProperties world;
    private final Registry<Biome> biomeRegistry = RegistryFetcher.biomeRegistry();

    public NMSBiomeProvider(BiomeProvider delegate, long seed) {
        this(delegate, seed, null, null, null, null, "");
    }

    public NMSBiomeProvider(BiomeProvider delegate, long seed, org.terra2.adapter.terra.CoreTerraGenerator core,
                            com.dfsek.terra.api.world.info.WorldProperties world,
                            com.dfsek.terra.bukkit.compatibility.BiomeCompatibility compatibility, java.util.UUID worldId, String packId) {
        super();
        this.compatibility = compatibility; this.worldId = worldId; this.packId = packId;
        this.core = core;
        this.world = world;
        this.delegate = delegate;
        this.seed = seed;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return delegate.stream().map(this::key).distinct().map(biomeRegistry::getOrThrow);
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
        return biomeRegistry.getOrThrow(key(original(x << 2, y << 2, z << 2)));
    }
    public boolean vanillaCompatible() { return compatibility != null && compatibility.vanilla; }
    private com.dfsek.terra.api.world.biome.Biome original(int x, int y, int z) {
        return core == null ? delegate.getBiome(x, y, z, seed) : core.getBiome(world, x, y, z);
    }
    private net.minecraft.resources.ResourceKey<Biome> key(com.dfsek.terra.api.world.biome.Biome biome) {
        if(vanillaCompatible()) {
            var ref = compatibility.reference(biome).getKey();
            return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(ref.getNamespace(), ref.getKey()));
        }
        return ((BukkitPlatformBiome) biome.getPlatformBiome()).getContext().get(NMSBiomeInfo.class).biomeKey();
    }
    public void persist(net.minecraft.world.level.chunk.ChunkAccess chunk) {
        if(!vanillaCompatible()) return;
        int min = chunk.getMinY(), height = chunk.getHeight();
        var palette = new java.util.ArrayList<org.terra2.api.biome.BiomeIdentity>();
        var indexes = new java.util.HashMap<org.terra2.api.biome.BiomeIdentity,Integer>();
        int[] cells = new int[height * 4]; int index = 0;
        int bx = chunk.getPos().x() << 4, bz = chunk.getPos().z() << 4;
        for(int y=min;y<min+height;y+=4) for(int z=0;z<16;z+=4) for(int x=0;x<16;x+=4) {
            var identity = compatibility.identity(original(bx+x,y,bz+z));
            Integer entry=indexes.get(identity);
            if(entry==null) { entry=palette.size(); indexes.put(identity,entry);palette.add(identity); }
            cells[index++]=entry;
        }
        try {
            var data = new com.dfsek.terra.bukkit.compatibility.BiomeIdentityChunk(worldId, seed,
                chunk.getPos().x(), chunk.getPos().z(), min, height, packId, compatibility.fingerprint, palette, cells);
            chunk.persistentDataContainer.set(com.dfsek.terra.bukkit.compatibility.PersistedBiomeService.KEY,
                org.bukkit.persistence.PersistentDataType.BYTE_ARRAY,data.encode());
        } catch(java.io.IOException failed) { throw new IllegalStateException("Cannot persist Terra2 biome identity", failed); }
    }
}
