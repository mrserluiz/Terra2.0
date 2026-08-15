package com.dfsek.terra.lib.commons.io.file;

import com.dfsek.terra.lib.commons.io.build.AbstractSupplier;
import com.dfsek.terra.lib.commons.io.function.IOBiFunction;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.Objects;

public abstract class SimplePathVisitor extends SimpleFileVisitor<Path> implements PathVisitor {
   private final IOBiFunction<Path, IOException, FileVisitResult> visitFileFailedFunction;

   protected SimplePathVisitor() {
      this.visitFileFailedFunction = (x$0, x$1) -> super.visitFileFailed(x$0, x$1);
   }

   SimplePathVisitor(SimplePathVisitor.AbstractBuilder<?, ?> builder) {
      this.visitFileFailedFunction = builder.visitFileFailedFunction != null ? builder.visitFileFailedFunction : (x$0, x$1) -> super.visitFileFailed(x$0, x$1);
   }

   protected SimplePathVisitor(IOBiFunction<Path, IOException, FileVisitResult> visitFileFailedFunction) {
      this.visitFileFailedFunction = Objects.requireNonNull(visitFileFailedFunction, "visitFileFailedFunction");
   }

   public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      return this.visitFileFailedFunction.apply(file, exc);
   }

   protected abstract static class AbstractBuilder<T, B extends AbstractSupplier<T, B>> extends AbstractSupplier<T, B> {
      private IOBiFunction<Path, IOException, FileVisitResult> visitFileFailedFunction;

      public AbstractBuilder() {
      }

      IOBiFunction<Path, IOException, FileVisitResult> getVisitFileFailedFunction() {
         return this.visitFileFailedFunction;
      }

      public B setVisitFileFailedFunction(IOBiFunction<Path, IOException, FileVisitResult> visitFileFailedFunction) {
         this.visitFileFailedFunction = visitFileFailedFunction;
         return this.asThis();
      }
   }
}
