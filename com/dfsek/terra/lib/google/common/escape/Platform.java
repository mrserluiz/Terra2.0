package com.dfsek.terra.lib.google.common.escape;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.Objects;

@GwtCompatible(emulated = true)
final class Platform {
   private static final ThreadLocal<char[]> DEST_TL = new ThreadLocal<char[]>() {
      protected char[] initialValue() {
         return new char[1024];
      }
   };

   private Platform() {
   }

   static char[] charBufferFromThreadLocal() {
      return Objects.requireNonNull(DEST_TL.get());
   }
}
