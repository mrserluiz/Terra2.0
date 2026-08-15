package com.dfsek.terra.api.structure;

import com.dfsek.terra.api.util.Rotation;
import com.dfsek.terra.api.util.vector.Vector3Int;
import com.dfsek.terra.api.world.WritableWorld;
import java.util.Random;

public interface Structure {
   boolean generate(Vector3Int var1, WritableWorld var2, Random var3, Rotation var4);
}
