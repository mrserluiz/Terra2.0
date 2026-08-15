package com.dfsek.terra.lib.paperlib.features.asyncchunks;

import com.dfsek.terra.lib.paperlib.PaperLib;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.World.ChunkLoadCallback;

public class AsyncChunksPaper_9_12 implements AsyncChunks {
   @Override
   public CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z, boolean gen, boolean isUrgent) {
      CompletableFuture<Chunk> future = new CompletableFuture<>();
      if (!gen && PaperLib.getMinecraftVersion() >= 12 && !world.isChunkGenerated(x, z)) {
         future.complete(null);
      } else {
         ChunkLoadCallback chunkLoadCallback = future::complete;
         world.getChunkAtAsync(x, z, chunkLoadCallback);
      }

      return future;
   }
}
