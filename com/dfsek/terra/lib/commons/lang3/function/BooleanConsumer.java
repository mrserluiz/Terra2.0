package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface BooleanConsumer {
   BooleanConsumer NOP = t -> {};

   static BooleanConsumer nop() {
      return NOP;
   }

   void accept(boolean var1);

   default BooleanConsumer andThen(BooleanConsumer after) {
      Objects.requireNonNull(after);
      return t -> {
         this.accept(t);
         after.accept(t);
      };
   }
}
