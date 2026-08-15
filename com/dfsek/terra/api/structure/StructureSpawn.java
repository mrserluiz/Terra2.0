package com.dfsek.terra.api.structure;

import com.dfsek.terra.api.util.vector.Vector3;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface StructureSpawn {
   Vector3 getNearestSpawn(int var1, int var2, long var3);
}
