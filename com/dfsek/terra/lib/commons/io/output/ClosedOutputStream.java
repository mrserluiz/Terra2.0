package com.dfsek.terra.lib.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

public class ClosedOutputStream extends OutputStream {
   public static final ClosedOutputStream INSTANCE = new ClosedOutputStream();
   @Deprecated
   public static final ClosedOutputStream CLOSED_OUTPUT_STREAM = INSTANCE;

   @Override
   public void flush() throws IOException {
      throw new IOException("flush() failed: stream is closed");
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      throw new IOException("write(byte[], int, int) failed: stream is closed");
   }

   @Override
   public void write(int b) throws IOException {
      throw new IOException("write(int) failed: stream is closed");
   }
}
