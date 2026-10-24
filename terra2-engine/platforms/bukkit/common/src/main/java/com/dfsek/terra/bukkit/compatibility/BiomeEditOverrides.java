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
        return edit(previous,world,seed,cx,cz,minY,height,pack,fingerprint,edits,null);
    }
    public static BiomeIdentityChunk edit(byte[] previous,UUID world,long seed,int cx,int cz,int minY,int height,
            String pack,String fingerprint,Map<Integer,BiomeIdentity> edits,Collection<BiomeIdentity> current) throws java.io.IOException {
        var values=new ArrayList<BiomeIdentity>();values.add(UNSET);int[] cells=new int[height*4];
        if(previous!=null) {
            var old=BiomeIdentityChunk.decode(previous);
            validate(old,world,seed,cx,cz,minY,height,pack,fingerprint,current);
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
    /** A pack version/color change may keep explicit edits, but never invent identities from a new provider. */
    public static void validate(BiomeIdentityChunk data,UUID world,long seed,int x,int z,int min,int height,
            String pack,String fingerprint,Collection<BiomeIdentity> current) throws java.io.IOException {
        if(!data.world.equals(world)||data.seed!=seed||data.x!=x||data.z!=z||data.minY!=min||data.height!=height||!data.pack.equals(pack))
            throw new java.io.IOException("Biome edit overlay belongs to another world, chunk or pack");
        if(current==null) {
            if(!data.fingerprint.equals(fingerprint))throw new java.io.IOException("Biome edit fingerprint changed without an identity catalog");
        } else for(var id:data.palette()) {
            if(!id.equals(UNSET) && !current.contains(id))throw new java.io.IOException("Saved edited biome is missing or changed: "+id.registeredId());
        }
    }
    public static BiomeIdentityChunk read(org.bukkit.Chunk chunk) throws java.io.IOException {
        var world=chunk.getWorld();
        if(!(world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper) || wrapper.biomeCompatibility()==null)
            throw new java.io.IOException("World is not bound to Terra2");
        var data=BiomeIdentityChunk.decode(chunk.getPersistentDataContainer().get(KEY,PersistentDataType.BYTE_ARRAY));
        validate(data,world.getUID(),world.getSeed(),chunk.getX(),chunk.getZ(),world.getMinHeight(),world.getMaxHeight()-world.getMinHeight(),
            wrapper.getPack().getID(),wrapper.biomeCompatibility().fingerprint,wrapper.biomeCompatibility().identities());
        return data;
    }
    public static boolean isUnset(BiomeIdentity identity) { return UNSET.equals(identity); }
    public static Optional<BiomeIdentity> at(World world,int x,int y,int z) {
        if(!world.isChunkLoaded(x>>4,z>>4) || y<world.getMinHeight() || y>=world.getMaxHeight())return Optional.empty();
        try {return read(world.getChunkAt(x>>4,z>>4)).at(x,y,z).filter(id->!isUnset(id));}
        catch(java.io.IOException invalid){return Optional.empty();}
    }
}
