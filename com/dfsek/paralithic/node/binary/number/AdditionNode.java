package com.dfsek.paralithic.node.binary.number;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.binary.BinaryNode;
import com.dfsek.paralithic.node.binary.CommutativeBinaryNode;
import com.dfsek.terra.lib.asm.MethodVisitor;

public class AdditionNode extends CommutativeBinaryNode {
   public AdditionNode(Node left, Node right) {
      super(left, right);
   }

   @Override
   protected BinaryNode newInstance(Node left, Node right) {
      return new AdditionNode(left, right);
   }

   @Override
   public void applyOperand(MethodVisitor visitor, String generatedImplementationName) {
      visitor.visitInsn(99);
   }

   @Override
   public BinaryNode.Op getOp() {
      return BinaryNode.Op.ADD;
   }

   public Constant constantSimplify() {
      return Constant.of(((Constant)this.left).getValue() + ((Constant)this.right).getValue());
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return this.left.eval(localVariables, inputs) + this.right.eval(localVariables, inputs);
   }
}
