package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class CountingInputStream extends ProxyInputStream {
   private long count;

   public CountingInputStream(InputStream in) {
      super(in);
   }

   CountingInputStream(InputStream in, ProxyInputStream.AbstractBuilder<?, ?> builder) {
      super(in, builder);
   }

   CountingInputStream(ProxyInputStream.AbstractBuilder<?, ?> builder) throws IOException {
      super(builder);
   }

   @Override
   protected synchronized void afterRead(int n) throws IOException {
      if (n != -1) {
         this.count += n;
      }

      super.afterRead(n);
   }

   public synchronized long getByteCount() {
      return this.count;
   }

   @Deprecated
   public int getCount() {
      long result = this.getByteCount();
      if (result > 2147483647L) {
         throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
      } else {
         return (int)result;
      }
   }

   public synchronized long resetByteCount() {
      long tmp = this.count;
      this.count = 0L;
      return tmp;
   }

   @Deprecated
   public int resetCount() {
      long result = this.resetByteCount();
      if (result > 2147483647L) {
         throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
      } else {
         return (int)result;
      }
   }

   @Override
   public synchronized long skip(long length) throws IOException {
      long skip = super.skip(length);
      this.count += skip;
      return skip;
   }
}
