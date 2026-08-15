package com.dfsek.paralithic.node.special;

import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class LocalVariableNode implements Node {
   private final int index;

   public LocalVariableNode(int index) {
      this.index = index;
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      visitor.visitVarInsn(24, NodeUtils.getLocalVariableIndex(this.index));
   }

   @Override
   public Statefulness statefulness() {
      return Statefulness.STATELESS;
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return localVariables[this.index];
   }
}
