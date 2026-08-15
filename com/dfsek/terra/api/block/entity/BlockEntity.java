package com.dfsek.terra.api.block.entity;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.util.vector.Vector3;

public interface BlockEntity extends Handle {
   boolean update(boolean var1);

   default void applyState(String state) {
   }

   Vector3 getPosition();

   int getX();

   int getY();

   int getZ();

   BlockState getBlockState();
}
