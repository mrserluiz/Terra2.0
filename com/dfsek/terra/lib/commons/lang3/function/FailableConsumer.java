package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface FailableConsumer<T, E extends Throwable> {
   FailableConsumer NOP = Function.identity()::apply;

   static <T, E extends Throwable> FailableConsumer<T, E> nop() {
      return NOP;
   }

   void accept(T var1) throws E;

   default FailableConsumer<T, E> andThen(FailableConsumer<? super T, E> after) {
      Objects.requireNonNull(after);
      return t -> {
         this.accept(t);
         after.accept(t);
      };
   }
}
