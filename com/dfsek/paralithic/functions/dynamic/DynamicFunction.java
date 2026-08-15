package com.dfsek.paralithic.functions.dynamic;

import com.dfsek.paralithic.functions.Function;
import org.jetbrains.annotations.Nullable;

public interface DynamicFunction extends Function {
   double eval(double... var1);

   default double eval(@Nullable Context context, double... args) {
      return this.eval(args);
   }
}
