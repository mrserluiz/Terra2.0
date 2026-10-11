package com.dfsek.terra.bukkit.compatibility;

import java.util.*;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;
import org.terra2.api.biome.*;

/** No chunk loading, biome writes or reconstruction from the current pack. */
public final class PersistedBiomeService implements WorldBiomeService {
    public static final NamespacedKey KEY=new NamespacedKey("terra2", "biome_identity_v1");
    public int apiVersion(){return 1;}
    public Optional<BiomeIdentity> at(World world,int x,int y,int z) {
        if(world==null || y<world.getMinHeight() || y>=world.getMaxHeight() || !world.isChunkLoaded(x>>4,z>>4))return Optional.empty();
        if(!(world.getGenerator() instanceof com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper wrapper)
                || wrapper.biomeCompatibility()==null || !wrapper.biomeCompatibility().vanilla)return Optional.empty();
        var bytes=world.getChunkAt(x>>4,z>>4).getPersistentDataContainer().get(KEY,PersistentDataType.BYTE_ARRAY);
        try {
            var data=BiomeIdentityChunk.decode(bytes);
            if(data.minY!=world.getMinHeight() || data.height!=world.getMaxHeight()-world.getMinHeight()
                    || !data.world.equals(world.getUID()) || data.seed!=world.getSeed() || data.x!=(x>>4) || data.z!=(z>>4)
                    || !data.fingerprint.equals(wrapper.biomeCompatibility().fingerprint))return Optional.empty();
            return data.at(x,y,z);
        }catch(java.io.IOException invalid){return Optional.empty();}
    }
}
