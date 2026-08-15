package com.dfsek.terra.api.world.chunk;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface ChunkAccess extends Handle {
   void setBlock(int var1, int var2, int var3, @NotNull BlockState var4);

   @NotNull
   BlockState getBlock(int var1, int var2, int var3);
}
