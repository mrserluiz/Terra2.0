package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface IOBooleanSupplier {
   default BooleanSupplier asBooleanSupplier() {
      return () -> Uncheck.getAsBoolean(this);
   }

   boolean getAsBoolean() throws IOException;
}
