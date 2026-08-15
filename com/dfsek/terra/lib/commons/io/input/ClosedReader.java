package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.io.Reader;

public class ClosedReader extends Reader {
   public static final ClosedReader INSTANCE = new ClosedReader();
   @Deprecated
   public static final ClosedReader CLOSED_READER = INSTANCE;

   @Override
   public void close() throws IOException {
   }

   @Override
   public int read(char[] cbuf, int off, int len) {
      return -1;
   }
}
