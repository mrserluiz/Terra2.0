package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.function.Erase;
import java.io.IOException;
import java.io.Reader;
import java.util.function.Supplier;

public class BrokenReader extends Reader {
   public static final BrokenReader INSTANCE = new BrokenReader();
   private final Supplier<Throwable> exceptionSupplier;

   public BrokenReader() {
      this(() -> new IOException("Broken reader"));
   }

   @Deprecated
   public BrokenReader(IOException exception) {
      this(() -> exception);
   }

   public BrokenReader(Supplier<Throwable> exceptionSupplier) {
      this.exceptionSupplier = exceptionSupplier;
   }

   public BrokenReader(Throwable exception) {
      this(() -> exception);
   }

   @Override
   public void close() throws IOException {
      throw this.rethrow();
   }

   @Override
   public void mark(int readAheadLimit) throws IOException {
      throw this.rethrow();
   }

   @Override
   public int read(char[] cbuf, int off, int len) throws IOException {
      throw this.rethrow();
   }

   @Override
   public boolean ready() throws IOException {
      throw this.rethrow();
   }

   @Override
   public void reset() throws IOException {
      throw this.rethrow();
   }

   private RuntimeException rethrow() {
      return Erase.rethrow(this.exceptionSupplier.get());
   }

   @Override
   public long skip(long n) throws IOException {
      throw this.rethrow();
   }
}
