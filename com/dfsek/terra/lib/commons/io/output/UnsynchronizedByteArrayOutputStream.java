package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.function.Uncheck;
import com.dfsek.terra.lib.commons.io.input.UnsynchronizedByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class UnsynchronizedByteArrayOutputStream extends AbstractByteArrayOutputStream<UnsynchronizedByteArrayOutputStream> {
   public static UnsynchronizedByteArrayOutputStream.Builder builder() {
      return new UnsynchronizedByteArrayOutputStream.Builder();
   }

   public static InputStream toBufferedInputStream(InputStream input) throws IOException {
      return toBufferedInputStream(input, 1024);
   }

   public static InputStream toBufferedInputStream(InputStream input, int size) throws IOException {
      UnsynchronizedByteArrayOutputStream output = builder().setBufferSize(size).get();

      InputStream var3;
      try {
         output.write(input);
         var3 = output.toInputStream();
      } catch (Throwable var6) {
         if (output != null) {
            try {
               output.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (output != null) {
         output.close();
      }

      return var3;
   }

   @Deprecated
   public UnsynchronizedByteArrayOutputStream() {
      this(1024);
   }

   @Deprecated
   public UnsynchronizedByteArrayOutputStream(int size) {
      if (size < 0) {
         throw new IllegalArgumentException("Negative initial size: " + size);
      }

      this.needNewBuffer(size);
   }

   @Override
   public void reset() {
      this.resetImpl();
   }

   @Override
   public int size() {
      return this.count;
   }

   @Override
   public byte[] toByteArray() {
      return this.toByteArrayImpl();
   }

   @Override
   public InputStream toInputStream() {
      return this.toInputStream(
         (buffer, offset, length) -> Uncheck.get(
            () -> UnsynchronizedByteArrayInputStream.builder().setByteArray(buffer).setOffset(offset).setLength(length).get()
         )
      );
   }

   @Override
   public void write(byte[] b, int off, int len) {
      if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
         throw new IndexOutOfBoundsException(String.format("offset=%,d, length=%,d", off, len));
      }

      if (len != 0) {
         this.writeImpl(b, off, len);
      }
   }

   @Override
   public int write(InputStream in) throws IOException {
      return this.writeImpl(in);
   }

   @Override
   public void write(int b) {
      this.writeImpl(b);
   }

   @Override
   public void writeTo(OutputStream out) throws IOException {
      this.writeToImpl(out);
   }

   public static class Builder extends AbstractStreamBuilder<UnsynchronizedByteArrayOutputStream, UnsynchronizedByteArrayOutputStream.Builder> {
      public UnsynchronizedByteArrayOutputStream get() {
         return new UnsynchronizedByteArrayOutputStream(this.getBufferSize());
      }
   }
}
