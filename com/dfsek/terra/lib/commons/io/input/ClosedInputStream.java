package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

public class ClosedInputStream extends InputStream {
   public static final ClosedInputStream INSTANCE = new ClosedInputStream();
   @Deprecated
   public static final ClosedInputStream CLOSED_INPUT_STREAM = INSTANCE;

   static InputStream ifNull(InputStream in) {
      return in != null ? in : INSTANCE;
   }

   @Override
   public int read() {
      return -1;
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      return -1;
   }
}
