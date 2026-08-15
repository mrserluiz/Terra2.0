package com.dfsek.paralithic.node.unary;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class NegationNode extends UnaryNode {
   public NegationNode(Node op) {
      super(op);
   }

   @Override
   public void applyOperand(MethodVisitor visitor) {
      visitor.visitInsn(119);
   }

   @NotNull
   @Override
   public Node simplify() {
      return this.op instanceof Constant ? Constant.of(-((Constant)this.op).getValue()) : super.simplify();
   }

   @Override
   public String toString() {
      return "-" + this.op.toString();
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return -this.op.eval(localVariables, inputs);
   }
}
