package com.dfsek.paralithic.node.binary;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Simplifiable;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.paralithic.util.Lazy;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class BinaryNode implements Simplifiable {
   protected Node left;
   protected Node right;
   private final Lazy<Statefulness> statefulness = Lazy.of(() -> Statefulness.combine(this.left.statefulness(), this.right.statefulness()));
   private boolean sealed = false;

   public BinaryNode(Node left, Node right) {
      this.left = left;
      this.right = right;
   }

   public abstract void applyOperand(MethodVisitor var1, String var2);

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      this.left.apply(visitor, generatedImplementationName);
      this.right.apply(visitor, generatedImplementationName);
      this.applyOperand(visitor, generatedImplementationName);
   }

   public void setLeft(Node left) {
      this.left = left;
   }

   public void setRight(Node right) {
      this.right = right;
   }

   public void seal() {
      this.sealed = true;
   }

   public boolean isSealed() {
      return this.sealed;
   }

   public Node getLeft() {
      return this.left;
   }

   public Node getRight() {
      return this.right;
   }

   public abstract BinaryNode.Op getOp();

   @Override
   public String toString() {
      return "(" + this.left.toString() + this.getOp().toString() + this.right.toString() + ")";
   }

   public abstract Node constantSimplify();

   public Node finalSimplify() {
      return this;
   }

   @NotNull
   @Override
   public Node simplify() {
      this.left = NodeUtils.simplify(this.left);
      this.right = NodeUtils.simplify(this.right);
      this.statefulness.invalidate();
      return this.left instanceof Constant && this.right instanceof Constant ? this.constantSimplify() : this.finalSimplify();
   }

   @Override
   public Statefulness statefulness() {
      return this.statefulness.get();
   }

   public enum Op {
      ADD(3, "+"),
      SUBTRACT(3, "-"),
      MULTIPLY(4, "*"),
      DIVIDE(4, "/"),
      MODULO(4, "%"),
      POWER(5, "^"),
      LT(2, "<"),
      LT_EQ(2, "<="),
      EQ(2, "="),
      GT_EQ(2, ">="),
      GT(2, ">"),
      NEQ(2, "!="),
      AND(1, "&&"),
      OR(1, "||");

      private final int priority;
      private final String op;

      Op(int priority, String op) {
         this.priority = priority;
         this.op = op;
      }

      public int getPriority() {
         return this.priority;
      }

      @Override
      public String toString() {
         return this.op;
      }
   }
}
