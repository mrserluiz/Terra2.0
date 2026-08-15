package com.dfsek.terra.lib.commons.io.filefilter;

import com.dfsek.terra.lib.commons.io.file.NoopPathVisitor;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import com.dfsek.terra.lib.commons.io.file.PathVisitor;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class PathVisitorFileFilter extends AbstractFileFilter {
   private final PathVisitor pathVisitor;

   public PathVisitorFileFilter(PathVisitor pathVisitor) {
      this.pathVisitor = pathVisitor == null ? NoopPathVisitor.INSTANCE : pathVisitor;
   }

   @Override
   public boolean accept(File file) {
      try {
         Path path = file.toPath();
         return this.visitFile(path, file.exists() ? PathUtils.readBasicFileAttributes(path) : null) == FileVisitResult.CONTINUE;
      } catch (IOException e) {
         return this.handle(e) == FileVisitResult.CONTINUE;
      }
   }

   @Override
   public boolean accept(File dir, String name) {
      try {
         Path path = dir.toPath().resolve(name);
         return this.accept(path, PathUtils.readBasicFileAttributes(path)) == FileVisitResult.CONTINUE;
      } catch (IOException e) {
         return this.handle(e) == FileVisitResult.CONTINUE;
      }
   }

   @Override
   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return this.get(() -> Files.isDirectory(path) ? this.pathVisitor.postVisitDirectory(path, null) : this.visitFile(path, attributes));
   }

   @Override
   public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
      return this.pathVisitor.visitFile(path, attributes);
   }
}
