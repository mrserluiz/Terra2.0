package com.dfsek.terra.api.world;

import com.dfsek.terra.api.block.entity.BlockEntity;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.util.vector.Vector3Int;

public interface ReadableWorld extends World {
   BlockState getBlockState(int var1, int var2, int var3);

   default BlockState getBlockState(Vector3 position) {
      return this.getBlockState(position.getBlockX(), position.getBlockY(), position.getBlockZ());
   }

   default BlockState getBlockState(Vector3Int position) {
      return this.getBlockState(position.getX(), position.getY(), position.getZ());
   }

   BlockEntity getBlockEntity(int var1, int var2, int var3);

   default BlockEntity getBlockEntity(Vector3 position) {
      return this.getBlockEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
   }

   default BlockEntity getBlockEntity(Vector3Int position) {
      return this.getBlockEntity(position.getX(), position.getY(), position.getZ());
   }
}
