package com.dfsek.terra.lib.commons.io.input.buffer;

import com.dfsek.terra.lib.commons.io.IOUtils;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CircularBufferInputStream extends FilterInputStream {
   protected final CircularByteBuffer buffer;
   protected final int bufferSize;
   private boolean eof;

   public CircularBufferInputStream(InputStream inputStream) {
      this(inputStream, 8192);
   }

   public CircularBufferInputStream(InputStream inputStream, int bufferSize) {
      super(Objects.requireNonNull(inputStream, "inputStream"));
      if (bufferSize <= 0) {
         throw new IllegalArgumentException("Illegal bufferSize: " + bufferSize);
      }

      this.buffer = new CircularByteBuffer(bufferSize);
      this.bufferSize = bufferSize;
      this.eof = false;
   }

   @Override
   public void close() throws IOException {
      super.close();
      this.eof = true;
      this.buffer.clear();
   }

   protected void fillBuffer() throws IOException {
      if (!this.eof) {
         int space = this.buffer.getSpace();
         byte[] buf = IOUtils.byteArray(space);

         while (space > 0) {
            int res = this.in.read(buf, 0, space);
            if (res == -1) {
               this.eof = true;
               return;
            }

            if (res > 0) {
               this.buffer.add(buf, 0, res);
               space -= res;
            }
         }
      }
   }

   protected boolean haveBytes(int count) throws IOException {
      if (this.buffer.getCurrentNumberOfBytes() < count) {
         this.fillBuffer();
      }

      return this.buffer.hasBytes();
   }

   @Override
   public int read() throws IOException {
      return !this.haveBytes(1) ? -1 : this.buffer.read() & 0xFF;
   }

   @Override
   public int read(byte[] targetBuffer, int offset, int length) throws IOException {
      Objects.requireNonNull(targetBuffer, "targetBuffer");
      if (offset < 0) {
         throw new IllegalArgumentException("Offset must not be negative");
      }

      if (length < 0) {
         throw new IllegalArgumentException("Length must not be negative");
      }

      if (!this.haveBytes(length)) {
         return -1;
      }

      int result = Math.min(length, this.buffer.getCurrentNumberOfBytes());

      for (int i = 0; i < result; i++) {
         targetBuffer[offset + i] = this.buffer.read();
      }

      return result;
   }
}
