package com.dfsek.terra.lib.commons.io.input;

public class InfiniteCircularInputStream extends CircularInputStream {
   public InfiniteCircularInputStream(byte[] repeatContent) {
      super(repeatContent, -1L);
   }
}
