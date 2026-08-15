package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

public class AutoCloseInputStream extends ProxyInputStream {
   public static AutoCloseInputStream.Builder builder() {
      return new AutoCloseInputStream.Builder();
   }

   private AutoCloseInputStream(AutoCloseInputStream.Builder builder) throws IOException {
      super(builder);
   }

   @Deprecated
   public AutoCloseInputStream(InputStream in) {
      super(ClosedInputStream.ifNull(in));
   }

   @Override
   protected void afterRead(int n) throws IOException {
      if (n == -1) {
         this.close();
      }

      super.afterRead(n);
   }

   @Override
   public void close() throws IOException {
      super.close();
      this.in = ClosedInputStream.INSTANCE;
   }

   @Override
   protected void finalize() throws Throwable {
      this.close();
      super.finalize();
   }

   public static class Builder extends ProxyInputStream.AbstractBuilder<AutoCloseInputStream, AutoCloseInputStream.Builder> {
      public AutoCloseInputStream get() throws IOException {
         return new AutoCloseInputStream(this);
      }
   }
}
