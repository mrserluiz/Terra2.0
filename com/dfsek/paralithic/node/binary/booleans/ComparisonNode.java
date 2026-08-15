package com.dfsek.paralithic.node.binary.booleans;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.binary.BinaryNode;
import com.dfsek.terra.lib.asm.Label;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class ComparisonNode extends BinaryNode {
   private final BinaryNode.Op op;

   public ComparisonNode(Node left, Node right, BinaryNode.Op op) {
      super(left, right);
      this.op = op;
   }

   private static int toInstruction(BinaryNode.Op op) {
      switch (op) {
         case EQ:
            return 153;
         case GT:
            return 157;
         case LT:
            return 155;
         case NEQ:
            return 154;
         case GT_EQ:
            return 156;
         case LT_EQ:
            return 158;
         default:
            throw new IllegalArgumentException("Not comparison: " + op);
      }
   }

   @Override
   public void applyOperand(MethodVisitor visitor, String generatedImplementationName) {
      Label endIf = new Label();
      Label valid = new Label();
      this.left.apply(visitor, generatedImplementationName);
      this.right.apply(visitor, generatedImplementationName);
      visitor.visitInsn(152);
      visitor.visitJumpInsn(toInstruction(this.op), valid);
      visitor.visitInsn(14);
      visitor.visitJumpInsn(167, endIf);
      visitor.visitLabel(valid);
      visitor.visitInsn(15);
      visitor.visitLabel(endIf);
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      this.applyOperand(visitor, generatedImplementationName);
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      double l = this.left.eval(localVariables, inputs);
      double r = this.right.eval(localVariables, inputs);
      switch (this.op) {
         case EQ:
            return l == r ? 1.0 : 0.0;
         case GT:
            return l > r ? 1.0 : 0.0;
         case LT:
            return l < r ? 1.0 : 0.0;
         case NEQ:
            return l != r ? 1.0 : 0.0;
         case GT_EQ:
            return l >= r ? 1.0 : 0.0;
         case LT_EQ:
            return l <= r ? 1.0 : 0.0;
         default:
            throw new IllegalArgumentException("Not comparison: " + this.op);
      }
   }

   @Override
   public BinaryNode.Op getOp() {
      return this.op;
   }

   @Override
   public Node constantSimplify() {
      double l = ((Constant)this.left).getValue();
      double r = ((Constant)this.right).getValue();
      switch (this.op) {
         case EQ:
            return Constant.of(l == r ? 1.0 : 0.0);
         case GT:
            return Constant.of(l > r ? 1.0 : 0.0);
         case LT:
            return Constant.of(l < r ? 1.0 : 0.0);
         case NEQ:
            return Constant.of(l != r ? 1.0 : 0.0);
         case GT_EQ:
            return Constant.of(l >= r ? 1.0 : 0.0);
         case LT_EQ:
            return Constant.of(l <= r ? 1.0 : 0.0);
         default:
            throw new IllegalArgumentException("Not comparison: " + this.op);
      }
   }
}
