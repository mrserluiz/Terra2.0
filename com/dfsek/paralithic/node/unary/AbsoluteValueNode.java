package com.dfsek.paralithic.node.unary;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.terra.lib.asm.Label;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class AbsoluteValueNode extends UnaryNode {
   public AbsoluteValueNode(Node op) {
      super(op);
   }

   @Override
   public void applyOperand(MethodVisitor visitor) {
      Label endIf = new Label();
      visitor.visitInsn(92);
      visitor.visitInsn(14);
      visitor.visitInsn(152);
      visitor.visitJumpInsn(156, endIf);
      visitor.visitInsn(119);
      visitor.visitLabel(endIf);
   }

   @NotNull
   @Override
   public Node simplify() {
      return this.op instanceof Constant ? Constant.of(Math.abs(((Constant)this.op).getValue())) : super.simplify();
   }

   @Override
   public String toString() {
      return "|" + this.op.toString() + "|";
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return Math.abs(this.op.eval(localVariables, inputs));
   }
}
