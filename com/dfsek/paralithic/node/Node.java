package com.dfsek.paralithic.node;

import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public interface Node {
   void apply(@NotNull MethodVisitor var1, String var2);

   Statefulness statefulness();

   double eval(double[] var1, double... var2);
}
