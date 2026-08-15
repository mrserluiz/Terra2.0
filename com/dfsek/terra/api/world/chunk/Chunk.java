package com.dfsek.terra.api.world.chunk;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public interface Chunk extends ChunkAccess {
   void setBlock(int var1, int var2, int var3, BlockState var4, boolean var5);

   @Override
   default void setBlock(int x, int y, int z, @NotNull BlockState data) {
      this.setBlock(x, y, z, data, false);
   }

   @NotNull
   @Override
   BlockState getBlock(int var1, int var2, int var3);

   int getX();

   int getZ();

   ServerWorld getWorld();
}
