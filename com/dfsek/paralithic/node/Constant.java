package com.dfsek.paralithic.node;

import com.dfsek.terra.lib.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

public class Constant implements Simplifiable {
   protected final double value;
   public static final Constant DCONST_0 = new Constant(0.0) {
      @Override
      public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
         visitor.visitInsn(14);
      }

      @NotNull
      @Override
      public Node simplify() {
         return this;
      }
   };
   public static final Constant DCONST_1 = new Constant(1.0) {
      @Override
      public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
         visitor.visitInsn(15);
      }

      @NotNull
      @Override
      public Node simplify() {
         return this;
      }
   };

   private Constant(double value) {
      this.value = value;
   }

   public static Constant of(double value) {
      return new Constant(value);
   }

   public double getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      return Double.toString(this.value);
   }

   @Override
   public void apply(@NotNull MethodVisitor visitor, String generatedImplementationName) {
      visitor.visitLdcInsn(this.value);
   }

   @Override
   public Statefulness statefulness() {
      return Statefulness.STATELESS;
   }

   @Override
   public double eval(double[] localVariables, double... inputs) {
      return this.value;
   }

   @NotNull
   @Override
   public Node simplify() {
      if (this.value == 0.0) {
         return DCONST_0;
      } else {
         return this.value == 1.0 ? DCONST_1 : this;
      }
   }
}
