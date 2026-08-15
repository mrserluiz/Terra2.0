package com.dfsek.paralithic.node.special;

import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class InvocationVariableNode implements Node {
   private final int index;

   public InvocationVariableNode(int index) {
      this.index = index;
   }

   @Override
   public String toString() {
      return "LOCAL_" + this.index;
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      visitor.visitVarInsn(25, 2);
      NodeUtils.siPush(visitor, this.index);
      visitor.visitInsn(49);
   }

   @Override
   public Statefulness statefulness() {
      return Statefulness.STATELESS;
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return inputs[this.index];
   }
}
