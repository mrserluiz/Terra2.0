package com.dfsek.terra.api.world.util;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.WritableWorld;

public interface WriteInterceptor {
   default void write(int x, int y, int z, BlockState block, WritableWorld world) {
      this.write(x, y, z, block, world, false);
   }

   void write(int var1, int var2, int var3, BlockState var4, WritableWorld var5, boolean var6);
}
