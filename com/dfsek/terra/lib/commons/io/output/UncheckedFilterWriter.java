package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class UncheckedFilterWriter extends FilterWriter {
   public static UncheckedFilterWriter.Builder builder() {
      return new UncheckedFilterWriter.Builder();
   }

   private UncheckedFilterWriter(UncheckedFilterWriter.Builder builder) throws IOException {
      super(builder.getWriter());
   }

   @Override
   public Writer append(char c) throws UncheckedIOException {
      return Uncheck.apply(x$0 -> super.append(x$0), c);
   }

   @Override
   public Writer append(CharSequence csq) throws UncheckedIOException {
      return Uncheck.apply(x$0 -> super.append(x$0), csq);
   }

   @Override
   public Writer append(CharSequence csq, int start, int end) throws UncheckedIOException {
      return Uncheck.apply((x$0, x$1, x$2) -> super.append(x$0, x$1, x$2), csq, start, end);
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
   public void write(char[] cbuf) throws UncheckedIOException {
      Uncheck.accept(x$0 -> super.write(x$0), cbuf);
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws UncheckedIOException {
      Uncheck.accept((x$0, x$1, x$2) -> super.write(x$0, x$1, x$2), cbuf, off, len);
   }

   @Override
   public void write(int c) throws UncheckedIOException {
      Uncheck.accept(x$0 -> super.write(x$0), c);
   }

   @Override
   public void write(String str) throws UncheckedIOException {
      Uncheck.accept(x$0 -> super.write(x$0), str);
   }

   @Override
   public void write(String str, int off, int len) throws UncheckedIOException {
      Uncheck.accept((x$0, x$1, x$2) -> super.write(x$0, x$1, x$2), str, off, len);
   }

   public static class Builder extends AbstractStreamBuilder<UncheckedFilterWriter, UncheckedFilterWriter.Builder> {
      public UncheckedFilterWriter get() throws IOException {
         return new UncheckedFilterWriter(this);
      }
   }
}
