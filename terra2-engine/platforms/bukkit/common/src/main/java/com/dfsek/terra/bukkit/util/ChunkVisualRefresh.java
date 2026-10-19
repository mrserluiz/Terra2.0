package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

/** Rate-limited packet resends of loaded, authorized chunks. No loads, saves or regeneration. */
public final class ChunkVisualRefresh {
    private record Key(UUID world,int x,int z) {}
    private final TerraBukkitPlugin plugin;private final Set<Key> queue=new LinkedHashSet<>();private BukkitTask task;
    public ChunkVisualRefresh(TerraBukkitPlugin plugin){this.plugin=plugin;}
    public void request(World world,int x,int z) {
        if(!world.isChunkLoaded(x,z) || queue.size()>=512)return;
        queue.add(new Key(world.getUID(),x,z));
        if(task==null)task=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,1L,1L);
    }
    public void around(org.bukkit.entity.Player player) {
        int x=player.getLocation().getBlockX()>>4,z=player.getLocation().getBlockZ()>>4;
        // A chunk resend includes every vertical section, not a player-height window.
        int radius=Math.max(1,Math.min(8,Math.min(player.getClientViewDistance(),player.getWorld().getViewDistance())));
        for(int ring=0;ring<=radius;ring++)for(int dx=-ring;dx<=ring;dx++)for(int dz=-ring;dz<=ring;dz++)
            if(Math.max(Math.abs(dx),Math.abs(dz))==ring)request(player.getWorld(),x+dx,z+dz);
    }
    private void tick() {
        var iterator=queue.iterator();int count=0;
        while(iterator.hasNext() && count++<2) {
            var key=iterator.next();iterator.remove();World world=Bukkit.getWorld(key.world());if(world==null || !world.isChunkLoaded(key.x(),key.z()))continue;
            try{plugin.assertGenerationAuthorized(world.getName(),"PACKS");world.refreshChunk(key.x(),key.z());}
            catch(IllegalArgumentException refused){/* Authorization may have changed while queued. */}
        }
        if(queue.isEmpty()){task.cancel();task=null;}
    }
    public void close(){queue.clear();if(task!=null){task.cancel();task=null;}}
}
