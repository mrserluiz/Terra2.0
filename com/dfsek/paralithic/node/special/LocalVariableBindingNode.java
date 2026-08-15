package com.dfsek.paralithic.node.special;

import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Simplifiable;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.paralithic.util.Lazy;
import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class LocalVariableBindingNode implements Simplifiable {
   private final int index;
   private Node boundExpression;
   private Node expression;
   private final Lazy<Statefulness> statefulness = Lazy.of(() -> Statefulness.combine(this.boundExpression.statefulness(), this.expression.statefulness()));

   public LocalVariableBindingNode(int index, Node boundExpression, Node expression) {
      this.index = index;
      this.boundExpression = boundExpression;
      this.expression = expression;
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      this.boundExpression.apply(visitor, generatedImplementationName);
      visitor.visitVarInsn(57, NodeUtils.getLocalVariableIndex(this.index));
      this.expression.apply(visitor, generatedImplementationName);
   }

   @Override
   public Statefulness statefulness() {
      return this.statefulness.get();
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      localVariables[this.index] = this.boundExpression.eval(localVariables, inputs);
      return this.expression.eval(localVariables, inputs);
   }

   @NotNull
   @Override
   public Node simplify() {
      this.boundExpression = NodeUtils.simplify(this.boundExpression);
      this.expression = NodeUtils.simplify(this.expression);
      this.statefulness.invalidate();
      return this.expression instanceof Constant ? this.expression : this;
   }
}
