package com.dfsek.terra.lib.commons.text.lookup;

final class IllegalArgumentExceptions {
   static IllegalArgumentException format(String format, Object... args) {
      return new IllegalArgumentException(String.format(format, args));
   }

   static IllegalArgumentException format(Throwable t, String format, Object... args) {
      return new IllegalArgumentException(String.format(format, args), t);
   }

   private IllegalArgumentExceptions() {
   }
}
