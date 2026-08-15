package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.function.Erase;
import java.io.IOException;
import java.io.Writer;
import java.util.function.Supplier;

public class BrokenWriter extends Writer {
   public static final BrokenWriter INSTANCE = new BrokenWriter();
   private final Supplier<Throwable> exceptionSupplier;

   public BrokenWriter() {
      this(() -> new IOException("Broken writer"));
   }

   @Deprecated
   public BrokenWriter(IOException exception) {
      this(() -> exception);
   }

   public BrokenWriter(Supplier<Throwable> exceptionSupplier) {
      this.exceptionSupplier = exceptionSupplier;
   }

   public BrokenWriter(Throwable exception) {
      this(() -> exception);
   }

   @Override
   public void close() throws IOException {
      throw this.rethrow();
   }

   @Override
   public void flush() throws IOException {
      throw this.rethrow();
   }

   private RuntimeException rethrow() {
      return Erase.rethrow(this.exceptionSupplier.get());
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws IOException {
      throw this.rethrow();
   }
}
