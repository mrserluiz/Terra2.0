package com.dfsek.terra.api.world.chunk.generation;

import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.api.world.WritableWorld;

public interface ProtoWorld extends WritableWorld {
   int centerChunkX();

   int centerChunkZ();

   ServerWorld getWorld();
}
