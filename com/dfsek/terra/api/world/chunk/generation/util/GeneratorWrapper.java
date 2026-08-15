package com.dfsek.terra.api.world.chunk.generation.util;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;

public interface GeneratorWrapper extends Handle {
   ChunkGenerator getHandle();
}
