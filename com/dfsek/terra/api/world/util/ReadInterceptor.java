package com.dfsek.terra.api.world.util;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.ReadableWorld;

public interface ReadInterceptor {
   BlockState read(int var1, int var2, int var3, ReadableWorld var4);
}
