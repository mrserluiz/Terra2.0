package com.dfsek.terra.lib.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class SymbolicLinkFileFilter extends AbstractFileFilter implements Serializable {
   public static final SymbolicLinkFileFilter INSTANCE = new SymbolicLinkFileFilter();
   private static final long serialVersionUID = 1L;

   protected SymbolicLinkFileFilter() {
   }

   public SymbolicLinkFileFilter(FileVisitResult onAccept, FileVisitResult onReject) {
      super(onAccept, onReject);
   }

   @Override
   public boolean accept(File file) {
      return this.isSymbolicLink(file.toPath());
   }

   @Override
   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return this.toFileVisitResult(this.isSymbolicLink(path));
   }

   boolean isSymbolicLink(Path filePath) {
      return Files.isSymbolicLink(filePath);
   }
}
