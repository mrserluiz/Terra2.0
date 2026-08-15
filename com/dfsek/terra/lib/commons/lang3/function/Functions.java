package com.dfsek.terra.lib.commons.lang3.function;

import java.util.function.Function;

public final class Functions {
   public static <T, R> R apply(Function<T, R> function, T object) {
      return function != null ? function.apply(object) : null;
   }

   public static <T, R> Function<T, R> function(Function<T, R> function) {
      return function;
   }

   private Functions() {
   }
}
