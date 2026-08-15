package com.dfsek.terra.api.handle;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.entity.EntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface WorldHandle {
   @NotNull
   @Contract("_ -> new")
   BlockState createBlockState(@NotNull String var1);

   @NotNull
   @Contract(pure = true)
   BlockState air();

   @NotNull
   EntityType getEntity(@NotNull String var1);
}
