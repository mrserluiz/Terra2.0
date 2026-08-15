package com.dfsek.terra.api.world.chunk.generation.util.provider;

import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;

public interface ChunkGeneratorProvider {
   ChunkGenerator newInstance(ConfigPack var1);
}
