package com.dfsek.terra.api.entity;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.world.ServerWorld;

public interface Entity extends Handle {
   Vector3 position();

   void position(Vector3 var1);

   void world(ServerWorld var1);

   ServerWorld world();
}
