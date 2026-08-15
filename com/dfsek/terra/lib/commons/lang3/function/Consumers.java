package com.dfsek.terra.lib.commons.lang3.function;

import java.util.function.Consumer;
import java.util.function.Function;

public class Consumers {
   private static final Consumer NOP = Function.identity()::apply;

   public static <T> void accept(Consumer<T> consumer, T object) {
      if (consumer != null) {
         consumer.accept(object);
      }
   }

   public static <T> Consumer<T> nop() {
      return NOP;
   }

   private Consumers() {
   }
}
