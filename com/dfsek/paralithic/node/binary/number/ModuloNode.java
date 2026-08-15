package com.dfsek.paralithic.node.binary.number;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.binary.BinaryNode;
import com.dfsek.terra.lib.asm.MethodVisitor;

public class ModuloNode extends BinaryNode {
   public ModuloNode(Node left, Node right) {
      super(left, right);
   }

   @Override
   public void applyOperand(MethodVisitor visitor, String generatedImplementationName) {
      visitor.visitInsn(115);
   }

   @Override
   public BinaryNode.Op getOp() {
      return BinaryNode.Op.MODULO;
   }

   public Constant constantSimplify() {
      return Constant.of(((Constant)this.left).getValue() % ((Constant)this.right).getValue());
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return this.left.eval(localVariables, inputs) % this.right.eval(localVariables, inputs);
   }
}
