package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Comparator;

@FunctionalInterface
public interface IOComparator<T> {
   default Comparator<T> asComparator() {
      return (t, u) -> Uncheck.compare(this, t, u);
   }

   int compare(T var1, T var2) throws IOException;
}
