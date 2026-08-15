package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.function.Erase;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.function.Supplier;

public class BrokenOutputStream extends OutputStream {
   public static final BrokenOutputStream INSTANCE = new BrokenOutputStream();
   private final Function<String, Throwable> exceptionFunction;

   public BrokenOutputStream() {
      this(m -> new IOException("Broken output stream: " + m));
   }

   @Deprecated
   public BrokenOutputStream(IOException exception) {
      this(m -> exception);
   }

   public BrokenOutputStream(Function<String, Throwable> exceptionFunction) {
      this.exceptionFunction = exceptionFunction;
   }

   @Deprecated
   public BrokenOutputStream(Supplier<Throwable> exceptionSupplier) {
      this.exceptionFunction = m -> exceptionSupplier.get();
   }

   public BrokenOutputStream(Throwable exception) {
      this(m -> exception);
   }

   @Override
   public void close() throws IOException {
      throw this.rethrow("close()");
   }

   @Override
   public void flush() throws IOException {
      throw this.rethrow("flush()");
   }

   private RuntimeException rethrow(String method) {
      return Erase.rethrow(this.exceptionFunction.apply(method));
   }

   @Override
   public void write(int b) throws IOException {
      throw this.rethrow("write(int)");
   }
}
