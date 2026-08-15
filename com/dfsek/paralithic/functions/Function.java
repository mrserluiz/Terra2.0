package com.dfsek.paralithic.functions;

import com.dfsek.paralithic.node.Statefulness;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Function {
   @Contract(pure = true)
   int getArgNumber();

   @NotNull
   @Contract(pure = true)
   Statefulness statefulness();
}
