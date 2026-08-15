package com.dfsek.terra.api.world.chunk.generation.util;

import com.dfsek.terra.api.block.state.BlockState;

public interface Palette {
   BlockState get(int var1, double var2, double var4, double var6, long var8);
}
