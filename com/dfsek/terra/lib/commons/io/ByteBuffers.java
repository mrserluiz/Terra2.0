package com.dfsek.terra.lib.commons.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ByteBuffers {
   public static ByteBuffer littleEndian(byte[] array) {
      return littleEndian(ByteBuffer.wrap(array));
   }

   public static ByteBuffer littleEndian(ByteBuffer allocate) {
      return allocate.order(ByteOrder.LITTLE_ENDIAN);
   }

   public static ByteBuffer littleEndian(int capacity) {
      return littleEndian(ByteBuffer.allocate(capacity));
   }

   private ByteBuffers() {
   }
}
