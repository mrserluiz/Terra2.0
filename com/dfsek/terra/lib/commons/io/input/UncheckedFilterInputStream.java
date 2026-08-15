package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class UncheckedFilterInputStream extends FilterInputStream {
   public static UncheckedFilterInputStream.Builder builder() {
      return new UncheckedFilterInputStream.Builder();
   }

   private UncheckedFilterInputStream(InputStream inputStream) {
      super(inputStream);
   }

   @Override
   public int available() throws UncheckedIOException {
      return Uncheck.getAsInt(() -> super.available());
   }

   @Override
   public void close() throws UncheckedIOException {
      Uncheck.run(() -> super.close());
   }

   @Override
   public int read() throws UncheckedIOException {
      return Uncheck.getAsInt(() -> super.read());
   }

   @Override
   public int read(byte[] b) throws UncheckedIOException {
      return Uncheck.<byte[], Integer>apply(x$0 -> super.read(x$0), b);
   }

   @Override
   public int read(byte[] b, int off, int len) throws UncheckedIOException {
      return Uncheck.<byte[], Integer, Integer, Integer>apply((x$0, x$1, x$2) -> super.read(x$0, x$1, x$2), b, off, len);
   }

   @Override
   public synchronized void reset() throws UncheckedIOException {
      Uncheck.run(() -> super.reset());
   }

   @Override
   public long skip(long n) throws UncheckedIOException {
      return Uncheck.<Long, Long>apply(x$0 -> super.skip(x$0), n);
   }

   public static class Builder extends AbstractStreamBuilder<UncheckedFilterInputStream, UncheckedFilterInputStream.Builder> {
      public UncheckedFilterInputStream get() {
         return Uncheck.get(() -> new UncheckedFilterInputStream(this.getInputStream()));
      }
   }
}
