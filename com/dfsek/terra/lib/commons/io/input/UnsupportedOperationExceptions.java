package com.dfsek.terra.lib.commons.io.input;

final class UnsupportedOperationExceptions {
   private static final String MARK_RESET = "mark/reset";

   static UnsupportedOperationException mark() {
      return method("mark/reset");
   }

   static UnsupportedOperationException method(String method) {
      return new UnsupportedOperationException(method + " not supported");
   }

   static UnsupportedOperationException reset() {
      return method("mark/reset");
   }
}
