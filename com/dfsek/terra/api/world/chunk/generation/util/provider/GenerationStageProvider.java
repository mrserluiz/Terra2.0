package com.dfsek.terra.api.world.chunk.generation.util.provider;

import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.chunk.generation.stage.GenerationStage;

public interface GenerationStageProvider {
   GenerationStage newInstance(ConfigPack var1);
}
