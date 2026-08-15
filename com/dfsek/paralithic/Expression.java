package com.dfsek.paralithic;

import com.dfsek.paralithic.functions.dynamic.Context;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface Expression {
   Context DEFAULT_CONTEXT = new Context() {};

   default double evaluate(double... args) {
      return this.evaluate(DEFAULT_CONTEXT, args);
   }

   @Contract(pure = true)
   double evaluate(@Nullable Context var1, double... var2);
}
