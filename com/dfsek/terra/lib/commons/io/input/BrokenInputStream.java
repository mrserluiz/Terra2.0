package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.function.Erase;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class BrokenInputStream extends InputStream {
   public static final BrokenInputStream INSTANCE = new BrokenInputStream();
   private final Supplier<Throwable> exceptionSupplier;

   public BrokenInputStream() {
      this(() -> new IOException("Broken input stream"));
   }

   @Deprecated
   public BrokenInputStream(IOException exception) {
      this(() -> exception);
   }

   public BrokenInputStream(Supplier<Throwable> exceptionSupplier) {
      this.exceptionSupplier = exceptionSupplier;
   }

   public BrokenInputStream(Throwable exception) {
      this(() -> exception);
   }

   @Override
   public int available() throws IOException {
      throw this.rethrow();
   }

   @Override
   public void close() throws IOException {
      throw this.rethrow();
   }

   Throwable getThrowable() {
      return this.exceptionSupplier.get();
   }

   @Override
   public int read() throws IOException {
      throw this.rethrow();
   }

   @Override
   public synchronized void reset() throws IOException {
      throw this.rethrow();
   }

   private RuntimeException rethrow() {
      return Erase.rethrow(this.getThrowable());
   }

   @Override
   public long skip(long n) throws IOException {
      throw this.rethrow();
   }
}
