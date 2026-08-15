package com.dfsek.paralithic.node.binary;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import org.jetbrains.annotations.NotNull;

public abstract class CommutativeBinaryNode extends BinaryNode {
   public CommutativeBinaryNode(Node left, Node right) {
      super(left, right);
   }

   @NotNull
   @Override
   public Node finalSimplify() {
      if (this.left instanceof Constant && this.getClass().isInstance(this.right)) {
         CommutativeBinaryNode rightBin = (CommutativeBinaryNode)this.right;
         if (rightBin.left instanceof Constant || rightBin.right instanceof Constant) {
            boolean cSide = rightBin.left instanceof Constant;
            Node simplified = NodeUtils.simplify(this.newInstance(this.left, cSide ? rightBin.left : rightBin.right));
            return NodeUtils.simplify(this.newInstance(simplified, cSide ? rightBin.right : rightBin.left));
         }
      }

      if (this.getClass().isInstance(this.left) && this.right instanceof Constant) {
         CommutativeBinaryNode leftBin = (CommutativeBinaryNode)this.left;
         if (leftBin.left instanceof Constant || leftBin.right instanceof Constant) {
            boolean cSide = leftBin.left instanceof Constant;
            Node simplified = NodeUtils.simplify(this.newInstance(cSide ? leftBin.left : leftBin.right, this.right));
            return NodeUtils.simplify(this.newInstance(cSide ? leftBin.right : leftBin.left, simplified));
         }
      }

      return this;
   }

   protected abstract BinaryNode newInstance(Node var1, Node var2);
}
