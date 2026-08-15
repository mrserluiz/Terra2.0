package com.dfsek.terra.api.world;

import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.world.chunk.Chunk;

public interface ServerWorld extends WritableWorld {
   Chunk getChunkAt(int var1, int var2);

   default Chunk getChunkAt(Vector3 location) {
      return this.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
   }
}
