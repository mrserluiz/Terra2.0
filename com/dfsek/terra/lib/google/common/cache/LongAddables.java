package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.concurrent.atomic.LongAdder;

@GwtCompatible(emulated = true)
final class LongAddables {
   public static LongAddable create() {
      return new LongAddables.JavaUtilConcurrentLongAdder();
   }

   private static final class JavaUtilConcurrentLongAdder extends LongAdder implements LongAddable {
      private JavaUtilConcurrentLongAdder() {
      }
   }
}
