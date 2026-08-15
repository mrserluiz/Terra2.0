package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IOBiConsumer<T, U> {
   static <T, U> IOBiConsumer<T, U> noop() {
      return Constants.IO_BI_CONSUMER;
   }

   void accept(T var1, U var2) throws IOException;

   default IOBiConsumer<T, U> andThen(IOBiConsumer<? super T, ? super U> after) {
      Objects.requireNonNull(after);
      return (t, u) -> {
         this.accept(t, u);
         after.accept(t, u);
      };
   }

   default BiConsumer<T, U> asBiConsumer() {
      return (t, u) -> Uncheck.accept(this, t, u);
   }
}
