package com.dfsek.paralithic.node.binary.number;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.binary.BinaryNode;
import com.dfsek.terra.lib.asm.MethodVisitor;

public class DivisionNode extends BinaryNode {
   public DivisionNode(Node left, Node right) {
      super(left, right);
   }

   @Override
   public void applyOperand(MethodVisitor visitor, String generatedImplementationName) {
      visitor.visitInsn(111);
   }

   @Override
   public BinaryNode.Op getOp() {
      return BinaryNode.Op.DIVIDE;
   }

   public Constant constantSimplify() {
      return Constant.of(((Constant)this.left).getValue() / ((Constant)this.right).getValue());
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return this.left.eval(localVariables, inputs) / this.right.eval(localVariables, inputs);
   }
}
