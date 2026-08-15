package com.dfsek.paralithic.node.special.function;

import com.dfsek.paralithic.functions.natives.NativeFunction;
import com.dfsek.paralithic.node.Constant;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.node.Simplifiable;
import com.dfsek.paralithic.node.Statefulness;
import com.dfsek.paralithic.util.Lazy;
import com.dfsek.terra.lib.asm.MethodVisitor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class NativeFunctionNode implements Simplifiable {
   private final NativeFunction function;
   private List<Node> args;
   private final Lazy<Statefulness> statefulness = Lazy.of(() -> Statefulness.combine(this.args.stream().map(Node::statefulness).toArray(Statefulness[]::new)));

   public NativeFunctionNode(NativeFunction function, List<Node> args) {
      this.function = function;
      this.args = args;
   }

   @NotNull
   @Override
   public Node simplify() {
      this.args = this.args.stream().map(NodeUtils::simplify).collect(Collectors.toList());
      this.statefulness.invalidate();
      if (this.args.stream().allMatch(op -> op instanceof Constant)) {
         Object[] arg = this.args.stream().mapToDouble(op -> ((Constant)op).getValue()).boxed().toArray();

         try {
            return Constant.of(((Number)this.function.getMethod().invoke(null, arg)).doubleValue());
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
         }
      } else {
         return this;
      }
   }

   @Override
   public String toString() {
      StringBuilder stringBuilder;
      try {
         stringBuilder = new StringBuilder(this.function.getMethod().getName()).append('(');
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }

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
      Method nativeMethod;
      try {
         nativeMethod = this.function.getMethod();
      } catch (NoSuchMethodException e) {
         throw new RuntimeException("Unable to fetch native method.", e);
      }

      Parameter[] params = nativeMethod.getParameters();
      if (this.args.size() != params.length) {
         throw new IllegalArgumentException("Arguments do not match. Expected " + params.length + ", found " + this.args.size());
      }

      StringBuilder signature = new StringBuilder("(");

      for (int i = 0; i < params.length; i++) {
         Class<?> type = params[i].getType();
         this.args.get(i).apply(visitor, generatedImplementationName);
         if (NodeUtils.isWeakInteger(type)) {
            visitor.visitInsn(135);
         } else if (NodeUtils.isFloat(type)) {
            visitor.visitInsn(141);
         } else if (NodeUtils.isLong(type)) {
            visitor.visitInsn(138);
         } else if (!NodeUtils.isDouble(type)) {
            throw new IllegalArgumentException("Illegal parameter type: " + params[i]);
         }

         signature.append(NodeUtils.getDescriptorCharacter(type));
      }

      signature.append(')');
      int castInsn = Integer.MIN_VALUE;
      Class<?> type = nativeMethod.getReturnType();
      if (NodeUtils.isWeakInteger(type)) {
         castInsn = 135;
      } else if (NodeUtils.isFloat(type)) {
         castInsn = 141;
      } else if (NodeUtils.isLong(type)) {
         castInsn = 138;
      } else if (!NodeUtils.isDouble(type)) {
         throw new IllegalArgumentException("Illegal return type: " + type);
      }

      signature.append(NodeUtils.getDescriptorCharacter(nativeMethod.getReturnType()));
      visitor.visitMethodInsn(184, nativeMethod.getDeclaringClass().getCanonicalName().replace('.', '/'), nativeMethod.getName(), signature.toString(), false);
      if (castInsn != Integer.MIN_VALUE) {
         visitor.visitInsn(castInsn);
      }
   }

   @Override
   public Statefulness statefulness() {
      return this.statefulness.get();
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      try {
         return (Double)this.function.getMethod().invoke(null, this.args.stream().map(a -> a.eval(localVariables, inputs)).toArray());
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }
}
