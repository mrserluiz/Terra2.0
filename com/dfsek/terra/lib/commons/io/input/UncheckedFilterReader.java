package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.FilterReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;

public final class UncheckedFilterReader extends FilterReader {
   public static UncheckedFilterReader.Builder builder() {
      return new UncheckedFilterReader.Builder();
   }

   private UncheckedFilterReader(Reader reader) {
      super(reader);
   }

   @Override
   public void close() throws UncheckedIOException {
      Uncheck.run(() -> super.close());
   }

   @Override
   public void mark(int readAheadLimit) throws UncheckedIOException {
      Uncheck.accept(x$0 -> super.mark(x$0), readAheadLimit);
   }

   @Override
   public int read() throws UncheckedIOException {
      return Uncheck.getAsInt(() -> super.read());
   }

   @Override
   public int read(char[] cbuf) throws UncheckedIOException {
      return Uncheck.<char[], Integer>apply(x$0 -> super.read(x$0), cbuf);
   }

   @Override
   public int read(char[] cbuf, int off, int len) throws UncheckedIOException {
      return Uncheck.<char[], Integer, Integer, Integer>apply((x$0, x$1, x$2) -> super.read(x$0, x$1, x$2), cbuf, off, len);
   }

   @Override
   public int read(CharBuffer target) throws UncheckedIOException {
      return Uncheck.<CharBuffer, Integer>apply(x$0 -> super.read(x$0), target);
   }

   @Override
   public boolean ready() throws UncheckedIOException {
      return Uncheck.getAsBoolean(() -> super.ready());
   }

   @Override
   public void reset() throws UncheckedIOException {
      Uncheck.run(() -> super.reset());
   }

   @Override
   public long skip(long n) throws UncheckedIOException {
      return Uncheck.<Long, Long>apply(x$0 -> super.skip(x$0), n);
   }

   public static class Builder extends AbstractStreamBuilder<UncheckedFilterReader, UncheckedFilterReader.Builder> {
      public UncheckedFilterReader get() {
         return Uncheck.get(() -> new UncheckedFilterReader(this.getReader()));
      }
   }
}
