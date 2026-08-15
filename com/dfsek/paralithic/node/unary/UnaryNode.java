package com.dfsek.paralithic.node.unary;

import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Simplifiable;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.paralithic.util.Lazy;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class UnaryNode implements Simplifiable {
   protected Node op;
   private final Lazy<Statefulness> statefulness = Lazy.of(() -> this.op.statefulness());

   protected UnaryNode(Node op) {
      this.op = op;
   }

   public abstract void applyOperand(MethodVisitor var1);

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      this.op.apply(visitor, generatedImplementationName);
      this.applyOperand(visitor);
   }

   @NotNull
   @Override
   public Node simplify() {
      this.op = NodeUtils.simplify(this.op);
      this.statefulness.invalidate();
      return this;
   }

   @Override
   public Statefulness statefulness() {
      return this.statefulness.get();
   }
}
