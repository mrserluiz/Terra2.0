package com.dfsek.paralithic.node.binary.booleans;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.binary.BinaryNode;
import com.dfsek.terra.lib.asm.Label;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class AndNode extends BinaryNode {
   public AndNode(Node left, Node right) {
      super(left, right);
   }

   @Override
   public void applyOperand(MethodVisitor visitor, String generatedImplementationName) {
      Label end = new Label();
      Label fail = new Label();
      this.left.apply(visitor, generatedImplementationName);
      visitor.visitInsn(14);
      visitor.visitInsn(152);
      visitor.visitJumpInsn(153, fail);
      this.right.apply(visitor, generatedImplementationName);
      visitor.visitInsn(14);
      visitor.visitInsn(152);
      visitor.visitJumpInsn(153, fail);
      visitor.visitInsn(15);
      visitor.visitJumpInsn(167, end);
      visitor.visitLabel(fail);
      visitor.visitInsn(14);
      visitor.visitLabel(end);
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      this.applyOperand(visitor, generatedImplementationName);
   }

   @Override
   public BinaryNode.Op getOp() {
      return BinaryNode.Op.AND;
   }

   @Override
   public Node constantSimplify() {
      return Constant.of(((Constant)this.left).getValue() != 0.0 && ((Constant)this.right).getValue() != 0.0 ? 1.0 : 0.0);
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return this.left.eval(localVariables, inputs) != 0.0 && this.right.eval(localVariables, inputs) != 0.0 ? 1.0 : 0.0;
   }
}
