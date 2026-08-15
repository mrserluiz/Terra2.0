package com.dfsek.terra.lib.commons.io.file;

import com.dfsek.terra.lib.commons.io.function.IOBiFunction;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;

public class NoopPathVisitor extends SimplePathVisitor {
   public static final NoopPathVisitor INSTANCE = new NoopPathVisitor();

   public NoopPathVisitor() {
   }

   public NoopPathVisitor(IOBiFunction<Path, IOException, FileVisitResult> visitFileFailed) {
      super(visitFileFailed);
   }
}
