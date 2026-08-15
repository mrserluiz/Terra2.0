package com.dfsek.terra.lib.commons.io;

import java.nio.ByteOrder;

public final class ByteOrderParser {
   public static ByteOrder parseByteOrder(String value) {
      if (ByteOrder.BIG_ENDIAN.toString().equals(value)) {
         return ByteOrder.BIG_ENDIAN;
      } else if (ByteOrder.LITTLE_ENDIAN.toString().equals(value)) {
         return ByteOrder.LITTLE_ENDIAN;
      } else {
         throw new IllegalArgumentException(
            "Unsupported byte order setting: " + value + ", expected one of " + ByteOrder.LITTLE_ENDIAN + ", " + ByteOrder.BIG_ENDIAN
         );
      }
   }

   private ByteOrderParser() {
   }
}
