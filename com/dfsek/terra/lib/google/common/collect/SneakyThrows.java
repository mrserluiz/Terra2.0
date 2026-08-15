package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
final class SneakyThrows<T extends Throwable> {
   @CanIgnoreReturnValue
   static Error sneakyThrow(Throwable t) {
      throw new SneakyThrows().throwIt(t);
   }

   private Error throwIt(Throwable t) throws T {
      throw t;
   }

   private SneakyThrows() {
   }
}
