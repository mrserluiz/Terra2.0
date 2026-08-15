package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.IOException;
import java.io.InputStream;

public class UnsynchronizedFilterInputStream extends InputStream {
   protected volatile InputStream inputStream;

   public static UnsynchronizedFilterInputStream.Builder builder() {
      return new UnsynchronizedFilterInputStream.Builder();
   }

   UnsynchronizedFilterInputStream(InputStream inputStream) {
      this.inputStream = inputStream;
   }

   @Override
   public int available() throws IOException {
      return this.inputStream.available();
   }

   @Override
   public void close() throws IOException {
      this.inputStream.close();
   }

   @Override
   public void mark(int readLimit) {
      this.inputStream.mark(readLimit);
   }

   @Override
   public boolean markSupported() {
      return this.inputStream.markSupported();
   }

   @Override
   public int read() throws IOException {
      return this.inputStream.read();
   }

   @Override
   public int read(byte[] buffer) throws IOException {
      return this.read(buffer, 0, buffer.length);
   }

   @Override
   public int read(byte[] buffer, int offset, int count) throws IOException {
      return this.inputStream.read(buffer, offset, count);
   }

   @Override
   public void reset() throws IOException {
      this.inputStream.reset();
   }

   @Override
   public long skip(long count) throws IOException {
      return this.inputStream.skip(count);
   }

   public static class Builder extends AbstractStreamBuilder<UnsynchronizedFilterInputStream, UnsynchronizedFilterInputStream.Builder> {
      public UnsynchronizedFilterInputStream get() throws IOException {
         return new UnsynchronizedFilterInputStream(this.getInputStream());
      }
   }
}
