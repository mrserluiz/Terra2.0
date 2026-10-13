package com.dfsek.terra.bukkit.bedrock;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.terra2.api.biome.WorldBiomeService;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.world.BukkitPlatformBiome;
import com.dfsek.terra.bukkit.world.BukkitBiomeInfo;
import java.util.Optional;

/** Owning world thread only. Missing persisted identity must not be reconstructed in projected worlds. */
public final class PositionVisualResolver {
    private PositionVisualResolver() {}
    public record Sample(String profile,String source) {}
    public static Optional<Sample> at(World world,int x,int y,int z) {
        if(y<world.getMinHeight() || y>=world.getMaxHeight() || !world.isChunkLoaded(x>>4,z>>4))return Optional.empty();
        if(!(world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper))return Optional.empty();
        var edit=com.dfsek.terra.bukkit.compatibility.BiomeEditOverrides.at(world,x,y,z);
        if(edit.isPresent())return edit.map(id->new Sample(id.registeredId(),"PERSISTED_EDIT"));
        if(wrapper.biomeCompatibility()!=null && wrapper.biomeCompatibility().vanilla) {
            var service=Bukkit.getServicesManager().load(WorldBiomeService.class);
            return service==null ? Optional.empty() : service.at(world,x,y,z).map(id->new Sample(id.registeredId(),"PERSISTED_IDENTITY"));
        }
        // CUSTOM worlds have no PDC palette. This is explicitly a prediction from their bound provider.
        var biome=wrapper.getPack().getBiomeProvider().getBiome(x,y,z,world.getSeed());
        var key=((BukkitPlatformBiome)biome.getPlatformBiome()).getContext().get(BukkitBiomeInfo.class).biomeKey();
        return Optional.of(new Sample(key.toString(),"BOUND_PROVIDER_PREDICTION"));
    }
}
