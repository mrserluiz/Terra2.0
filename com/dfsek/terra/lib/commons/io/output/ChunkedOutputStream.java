package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ChunkedOutputStream extends FilterOutputStream {
   private final int chunkSize;

   public static ChunkedOutputStream.Builder builder() {
      return new ChunkedOutputStream.Builder();
   }

   @Deprecated
   public ChunkedOutputStream(OutputStream stream) {
      this(stream, 8192);
   }

   @Deprecated
   public ChunkedOutputStream(OutputStream stream, int chunkSize) {
      super(stream);
      if (chunkSize <= 0) {
         throw new IllegalArgumentException("chunkSize <= 0");
      }

      this.chunkSize = chunkSize;
   }

   int getChunkSize() {
      return this.chunkSize;
   }

   @Override
   public void write(byte[] data, int srcOffset, int length) throws IOException {
      int bytes = length;
      int dstOffset = srcOffset;

      while (bytes > 0) {
         int chunk = Math.min(bytes, this.chunkSize);
         this.out.write(data, dstOffset, chunk);
         bytes -= chunk;
         dstOffset += chunk;
      }
   }

   public static class Builder extends AbstractStreamBuilder<ChunkedOutputStream, ChunkedOutputStream.Builder> {
      public ChunkedOutputStream get() throws IOException {
         return new ChunkedOutputStream(this.getOutputStream(), this.getBufferSize());
      }
   }
}
