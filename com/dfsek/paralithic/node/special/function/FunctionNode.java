package com.dfsek.paralithic.node.special.function;

import com.dfsek.paralithic.eval.ExpressionBuilder;
import com.dfsek.paralithic.functions.dynamic.DynamicFunction;
import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Simplifiable;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.paralithic.util.Lazy;
import com.dfsek.terra.lib.asm.MethodVisitor;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class FunctionNode implements Simplifiable {
   private List<Node> args;
   private DynamicFunction function;
   private final String fName;
   private final Lazy<Statefulness> statefulness = Lazy.of(
      () -> Statefulness.combine(Statefulness.combine(this.args.stream().map(Node::statefulness).toArray(Statefulness[]::new)), this.function.statefulness())
   );

   public FunctionNode(List<Node> args, DynamicFunction function, String fName) {
      this.args = args;
      this.function = function;
      this.fName = fName;
   }

   @Override
   public String toString() {
      StringBuilder stringBuilder = new StringBuilder(this.fName).append('(');

      for (int i = 0; i < this.args.size(); i++) {
         stringBuilder.append(this.args.get(i));
         if (i != this.args.size() - 1) {
            stringBuilder.append(", ");
         }
      }

      stringBuilder.append(')');
      return stringBuilder.toString();
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      visitor.visitVarInsn(25, 0);
      visitor.visitFieldInsn(180, generatedImplementationName, this.fName, "L" + ExpressionBuilder.DYNAMIC_FUNCTION_CLASS_NAME + ";");
      visitor.visitVarInsn(25, 1);
      NodeUtils.siPush(visitor, this.args.size());
      visitor.visitIntInsn(188, 7);

      for (int i = 0; i < this.args.size(); i++) {
         visitor.visitInsn(89);
         NodeUtils.siPush(visitor, i);
         this.args.get(i).apply(visitor, generatedImplementationName);
         visitor.visitInsn(82);
      }

      visitor.visitMethodInsn(185, ExpressionBuilder.DYNAMIC_FUNCTION_CLASS_NAME, "eval", "(L" + ExpressionBuilder.CONTEXT_CLASS_NAME + ";[D)D", true);
   }

   @Override
   public Statefulness statefulness() {
      return this.statefulness.get();
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return this.function.eval(this.args.stream().mapToDouble(a -> a.eval(localVariables, inputs)).toArray());
   }

   @NotNull
   @Override
   public Node simplify() {
      this.args = this.args.stream().map(NodeUtils::simplify).collect(Collectors.toList());
      this.statefulness.invalidate();
      return this.args.stream().allMatch(op -> op instanceof Constant) && this.function.statefulness() == Statefulness.STATELESS
         ? Constant.of(this.function.eval(this.args.stream().mapToDouble(op -> ((Constant)op).getValue()).toArray()))
         : this;
   }
}
