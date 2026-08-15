package com.dfsek.paralithic.node.binary.special;

import com.dfsek.paralithic.functions.natives.NativeMath;
import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.binary.BinaryNode;
import com.dfsek.paralithic.node.special.function.NativeFunctionNode;
import com.dfsek.terra.lib.asm.MethodVisitor;
import java.util.Arrays;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

public class PowerNode extends BinaryNode {
   public PowerNode(Node left, Node right) {
      super(left, right);
   }

   @Override
   public void applyOperand(MethodVisitor visitor, String generatedImplementationName) {
      new NativeFunctionNode(NativeMath.POW, Arrays.asList(this.left, this.right)).apply(visitor, generatedImplementationName);
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      this.applyOperand(visitor, generatedImplementationName);
   }

   @Override
   public BinaryNode.Op getOp() {
      return BinaryNode.Op.POWER;
   }

   @NotNull
   @Override
   public Node finalSimplify() {
      if (this.right instanceof Constant) {
         double pow = ((Constant)this.right).getValue();
         if (pow == 0.0) {
            return Constant.of(1.0);
         }

         if (pow == 1.0) {
            return this.left;
         }

         if (pow == 2.0) {
            return new NativeFunctionNode(NativeMath.POW2, Collections.singletonList(this.left));
         }

         if (pow == 0.5) {
            return new NativeFunctionNode(NativeMath.SQRT, Collections.singletonList(this.left));
         }

         if (pow > 0.0 && Math.floor(pow) == pow) {
            return new NativeFunctionNode(NativeMath.INT_POW, Arrays.asList(this.left, this.right));
         }
      }

      return this;
   }

   @Override
   public Node constantSimplify() {
      return Constant.of(Math.pow(((Constant)this.left).getValue(), ((Constant)this.right).getValue()));
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return Math.pow(this.left.eval(inputs), this.right.eval(inputs));
   }
}
