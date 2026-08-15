package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

@FunctionalInterface
public interface IOIntConsumer {
   IOIntConsumer NOOP = i -> {};

   void accept(int var1) throws IOException;

   default IOIntConsumer andThen(IOIntConsumer after) {
      Objects.requireNonNull(after);
      return i -> {
         this.accept(i);
         after.accept(i);
      };
   }

   default Consumer<Integer> asConsumer() {
      return i -> Uncheck.accept(this, i.intValue());
   }

   default IntConsumer asIntConsumer() {
      return i -> Uncheck.accept(this, i);
   }
}
