package com.dfsek.paralithic.functions.natives;

import com.dfsek.paralithic.node.Statefulness;
import org.jetbrains.annotations.NotNull;

public interface NativeMathFunction extends NativeFunction {
   @NotNull
   @Override
   default Statefulness statefulness() {
      return Statefulness.STATELESS;
   }

   @Override
   default int getArgNumber() {
      try {
         return this.getMethod().getParameterCount();
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }
}
