package com.dfsek.terra.bukkit.compatibility;

import org.bukkit.World;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.terra2.api.biome.BiomeIdentity;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import java.util.*;

/** Sparse edit overlay in chunk PDC. Unedited cells remain unknown, never reconstructed. */
public final class BiomeEditOverrides {
    public static final NamespacedKey KEY=new NamespacedKey("terra2","biome_edits_v1");
    private static final BiomeIdentity UNSET=new BiomeIdentity("__UNSET__","terra2:unset","minecraft:plains");
    private BiomeEditOverrides() {}
    public static BiomeIdentityChunk edit(byte[] previous,UUID world,long seed,int cx,int cz,int minY,int height,
            String pack,String fingerprint,Map<Integer,BiomeIdentity> edits) throws java.io.IOException {
        var values=new ArrayList<BiomeIdentity>();values.add(UNSET);int[] cells=new int[height*4];
        if(previous!=null) {
            var old=BiomeIdentityChunk.decode(previous);
            if(!matches(old,world,seed,cx,cz,minY,height,fingerprint))throw new java.io.IOException("Biome edit overlay does not belong to this world/pack");
            for(int y=minY;y<minY+height;y+=4)for(int z=0;z<16;z+=4)for(int x=0;x<16;x+=4) {
                int index=((y-minY)>>2)*16+(z>>2)*4+(x>>2);
                BiomeIdentity identity=old.at((cx<<4)+x,y,(cz<<4)+z).orElseThrow();
                if(!identity.equals(UNSET)){int id=values.indexOf(identity);if(id<0){id=values.size();values.add(identity);}cells[index]=id;}
            }
        }
        for(var e:edits.entrySet()) {
            if(e.getKey()<0 || e.getKey()>=cells.length || e.getValue().equals(UNSET))throw new IllegalArgumentException("Invalid biome edit cell");
            int id=values.indexOf(e.getValue());if(id<0){id=values.size();values.add(e.getValue());}cells[e.getKey()]=id;
        }
        return new BiomeIdentityChunk(world,seed,cx,cz,minY,height,pack,fingerprint,values,cells);
    }
    private static boolean matches(BiomeIdentityChunk data,UUID world,long seed,int x,int z,int min,int height,String fingerprint) {
        return data.world.equals(world)&&data.seed==seed&&data.x==x&&data.z==z&&data.minY==min&&data.height==height&&data.fingerprint.equals(fingerprint);
    }
    public static Optional<BiomeIdentity> at(World world,int x,int y,int z) {
        if(!world.isChunkLoaded(x>>4,z>>4) || y<world.getMinHeight() || y>=world.getMaxHeight())return Optional.empty();
        if(!(world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper) || wrapper.biomeCompatibility()==null)return Optional.empty();
        byte[] bytes=world.getChunkAt(x>>4,z>>4).getPersistentDataContainer().get(KEY,PersistentDataType.BYTE_ARRAY);
        try {
            var data=BiomeIdentityChunk.decode(bytes);
            if(!matches(data,world.getUID(),world.getSeed(),x>>4,z>>4,world.getMinHeight(),world.getMaxHeight()-world.getMinHeight(),wrapper.biomeCompatibility().fingerprint))return Optional.empty();
            return data.at(x,y,z).filter(id->!id.equals(UNSET));
        }catch(java.io.IOException invalid){return Optional.empty();}
    }
}
