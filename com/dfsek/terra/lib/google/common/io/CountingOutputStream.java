package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@J2ktIncompatible
@GwtIncompatible
public final class CountingOutputStream extends FilterOutputStream {
   private long count;

   public CountingOutputStream(OutputStream out) {
      super(Preconditions.checkNotNull(out));
   }

   public long getCount() {
      return this.count;
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      this.out.write(b, off, len);
      this.count += len;
   }

   @Override
   public void write(int b) throws IOException {
      this.out.write(b);
      this.count++;
   }

   @Override
   public void close() throws IOException {
      this.out.close();
   }
}
