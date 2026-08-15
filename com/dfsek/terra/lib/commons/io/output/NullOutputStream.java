package com.dfsek.terra.lib.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
   public static final NullOutputStream INSTANCE = new NullOutputStream();
   @Deprecated
   public static final NullOutputStream NULL_OUTPUT_STREAM = INSTANCE;

   @Override
   public void write(byte[] b) throws IOException {
   }

   @Override
   public void write(byte[] b, int off, int len) {
   }

   @Override
   public void write(int b) {
   }
}
