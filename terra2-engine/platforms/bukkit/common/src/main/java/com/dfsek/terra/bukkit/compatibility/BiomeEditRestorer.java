package com.dfsek.terra.bukkit.compatibility;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import java.io.IOException;
import java.util.*;

/** Reconciles only explicit saved Terra2 edits when their loaded chunk returns. Never samples terrain generation. */
public final class BiomeEditRestorer implements Listener,AutoCloseable {
    private record Key(UUID world,int x,int z) {}
    private record Cell(int x,int y,int z,org.bukkit.block.Biome target) {}
    private final TerraBukkitPlugin plugin;
    private final Set<Key> pending=new LinkedHashSet<>();
    private final Set<Key> warned=new LinkedHashSet<>();
    private BukkitTask task;
    public BiomeEditRestorer(TerraBukkitPlugin plugin){this.plugin=plugin;}
    @EventHandler(priority=EventPriority.MONITOR)
    public void loaded(ChunkLoadEvent event){enqueue(event.getChunk());}
    public void enqueue(Chunk chunk) {
        if(!Bukkit.isPrimaryThread() || Bukkit.getName().equalsIgnoreCase("Folia"))return;
        if(!chunk.getPersistentDataContainer().has(BiomeEditOverrides.KEY,PersistentDataType.BYTE_ARRAY))return;
        if(pending.size()>=512)return;
        pending.add(new Key(chunk.getWorld().getUID(),chunk.getX(),chunk.getZ()));
        if(task==null)task=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,1,1);
    }
    private void tick() {
        var iterator=pending.iterator();
        if(iterator.hasNext()) {
            var key=iterator.next();iterator.remove();var world=Bukkit.getWorld(key.world());
            if(world!=null && world.isChunkLoaded(key.x(),key.z())) {
                try{restore(world.getChunkAt(key.x(),key.z()));}
                catch(IOException | IllegalArgumentException refused) {
                    if(warned.add(key))plugin.getLogger().warning("Terra2 saved biome edit not restored in "+world.getName()+" ["+key.x()+","+key.z()+"]: "+refused.getMessage());
                    if(warned.size()>128)warned.remove(warned.iterator().next());
                }
            }
        }
        if(pending.isEmpty()){task.cancel();task=null;}
    }
    /** Resolves every destination before any write. Protected/foreign/unknown payloads are refused intact. */
    public int restore(Chunk chunk) throws IOException {
        if(!Bukkit.isPrimaryThread() || Bukkit.getName().equalsIgnoreCase("Folia"))throw new IllegalArgumentException("Paper world thread required");
        var world=chunk.getWorld();plugin.assertGenerationAuthorized(world.getName(),"PACKS");
        var data=BiomeEditOverrides.read(chunk);
        var wrapper=(BukkitChunkGeneratorWrapper)world.getGenerator();
        var targets=new HashMap<org.terra2.api.biome.BiomeIdentity,org.bukkit.block.Biome>();
        for(var id:data.palette())if(!BiomeEditOverrides.isUnset(id)) {
            var target=Registry.BIOME.get(NamespacedKey.fromString(wrapper.biomeCompatibility().vanilla?id.vanillaReference():id.registeredId()));
            if(target==null)throw new IOException("Unregistered saved biome: "+id.registeredId());
            targets.put(id,target);
        }
        var writes=new ArrayList<Cell>();
        int bx=chunk.getX()<<4,bz=chunk.getZ()<<4;
        for(int y=data.minY;y<data.minY+data.height;y+=4)for(int z=bz;z<bz+16;z+=4)for(int x=bx;x<bx+16;x+=4) {
            var id=data.at(x,y,z).orElseThrow();
            if(BiomeEditOverrides.isUnset(id))continue;
            var target=targets.get(id);
            if(!world.getBiome(x,y,z).equals(target))writes.add(new Cell(x,y,z,target));
        }
        for(var cell:writes)world.setBiome(cell.x(),cell.y(),cell.z(),cell.target());
        if(!writes.isEmpty())plugin.visualRefresh().request(world,chunk.getX(),chunk.getZ());
        return writes.size();
    }
    @Override public void close(){pending.clear();warned.clear();if(task!=null){task.cancel();task=null;}}
}
