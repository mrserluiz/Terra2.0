package com.dfsek.paralithic.node.special;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Simplifiable;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.paralithic.util.Lazy;
import com.dfsek.terra.lib.asm.Label;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class TernaryIfNode implements Simplifiable {
   private Node predicate;
   private Node left;
   private Node right;
   private final Lazy<Statefulness> statefulness = Lazy.of(
      () -> Statefulness.combine(this.predicate.statefulness(), this.left.statefulness(), this.right.statefulness())
   );

   public TernaryIfNode(Node predicate, Node left, Node right) {
      this.predicate = predicate;
      this.left = left;
      this.right = right;
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      Label equal = new Label();
      Label endIf = new Label();
      this.predicate.apply(visitor, generatedImplementationName);
      visitor.visitInsn(14);
      visitor.visitInsn(152);
      visitor.visitJumpInsn(153, equal);
      this.left.apply(visitor, generatedImplementationName);
      visitor.visitJumpInsn(167, endIf);
      visitor.visitLabel(equal);
      this.right.apply(visitor, generatedImplementationName);
      visitor.visitLabel(endIf);
   }

   @Override
   public Statefulness statefulness() {
      return this.statefulness.get();
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return this.predicate.eval(localVariables, inputs) != 0.0 ? this.left.eval(localVariables, inputs) : this.right.eval(localVariables, inputs);
   }

   @NotNull
   @Override
   public Node simplify() {
      this.predicate = NodeUtils.simplify(this.predicate);
      this.left = NodeUtils.simplify(this.left);
      this.right = NodeUtils.simplify(this.right);
      this.statefulness.invalidate();
      if (this.predicate instanceof Constant) {
         return ((Constant)this.predicate).getValue() != 0.0 ? this.left : this.right;
      } else {
         return this;
      }
   }

   @Override
   public String toString() {
      return "if(" + this.predicate + ", " + this.left + ", " + this.right + ")";
   }
}
