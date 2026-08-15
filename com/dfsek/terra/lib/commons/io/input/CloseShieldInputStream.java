package com.dfsek.terra.lib.commons.io.input;

import java.io.InputStream;

public class CloseShieldInputStream extends ProxyInputStream {
   public static InputStream systemIn(InputStream inputStream) {
      return inputStream == System.in ? wrap(inputStream) : inputStream;
   }

   public static CloseShieldInputStream wrap(InputStream inputStream) {
      return new CloseShieldInputStream(inputStream);
   }

   @Deprecated
   public CloseShieldInputStream(InputStream inputStream) {
      super(inputStream);
   }

   @Override
   public void close() {
      this.in = ClosedInputStream.INSTANCE;
   }
}
