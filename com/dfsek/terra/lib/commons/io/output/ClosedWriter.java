package com.dfsek.terra.lib.commons.io.output;

import java.io.IOException;
import java.io.Writer;

public class ClosedWriter extends Writer {
   public static final ClosedWriter INSTANCE = new ClosedWriter();
   @Deprecated
   public static final ClosedWriter CLOSED_WRITER = INSTANCE;

   @Override
   public void close() throws IOException {
   }

   @Override
   public void flush() throws IOException {
      throw new IOException("flush() failed: stream is closed");
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws IOException {
      throw new IOException("write(" + new String(cbuf) + ", " + off + ", " + len + ") failed: stream is closed");
   }
}
