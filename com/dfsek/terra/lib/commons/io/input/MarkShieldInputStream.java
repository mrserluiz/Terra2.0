package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

public class MarkShieldInputStream extends ProxyInputStream {
   public MarkShieldInputStream(InputStream in) {
      super(in);
   }

   @Override
   public void mark(int readLimit) {
   }

   @Override
   public boolean markSupported() {
      return false;
   }

   @Override
   public void reset() throws IOException {
      throw UnsupportedOperationExceptions.reset();
   }
}
