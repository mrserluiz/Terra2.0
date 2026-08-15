package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class UncheckedFilterOutputStream extends FilterOutputStream {
   public static UncheckedFilterOutputStream.Builder builder() {
      return new UncheckedFilterOutputStream.Builder();
   }

   private UncheckedFilterOutputStream(UncheckedFilterOutputStream.Builder builder) throws IOException {
      super(builder.getOutputStream());
   }

   @Override
   public void close() throws UncheckedIOException {
      Uncheck.run(() -> super.close());
   }

   @Override
   public void flush() throws UncheckedIOException {
      Uncheck.run(() -> super.flush());
   }

   @Override
   public void write(byte[] b) throws UncheckedIOException {
      Uncheck.accept(x$0 -> super.write(x$0), b);
   }

   @Override
   public void write(byte[] b, int off, int len) throws UncheckedIOException {
      Uncheck.accept((x$0, x$1, x$2) -> super.write(x$0, x$1, x$2), b, off, len);
   }

   @Override
   public void write(int b) throws UncheckedIOException {
      Uncheck.accept(x$0 -> super.write(x$0), b);
   }

   public static class Builder extends AbstractStreamBuilder<UncheckedFilterOutputStream, UncheckedFilterOutputStream.Builder> {
      public UncheckedFilterOutputStream get() throws IOException {
         return new UncheckedFilterOutputStream(this);
      }
   }
}
