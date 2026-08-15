package com.dfsek.terra.lib.commons.io.output;

import java.io.OutputStream;

public class CountingOutputStream extends ProxyOutputStream {
   private long count;

   public CountingOutputStream(OutputStream out) {
      super(out);
   }

   @Override
   protected synchronized void beforeWrite(int n) {
      this.count += n;
   }

   public synchronized long getByteCount() {
      return this.count;
   }

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

   public int resetCount() {
      long result = this.resetByteCount();
      if (result > 2147483647L) {
         throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
      } else {
         return (int)result;
      }
   }
}
