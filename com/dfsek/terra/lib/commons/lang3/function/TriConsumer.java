package com.dfsek.terra.lib.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<T, U, V> {
   void accept(T var1, U var2, V var3);

   default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
      Objects.requireNonNull(after);
      return (t, u, v) -> {
         this.accept(t, u, v);
         after.accept(t, u, v);
      };
   }
}
