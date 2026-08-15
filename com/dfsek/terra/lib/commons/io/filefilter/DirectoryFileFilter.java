package com.dfsek.terra.lib.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryFileFilter extends AbstractFileFilter implements Serializable {
   public static final IOFileFilter DIRECTORY = new DirectoryFileFilter();
   public static final IOFileFilter INSTANCE = DIRECTORY;
   private static final long serialVersionUID = -5148237843784525732L;

   protected DirectoryFileFilter() {
   }

   @Override
   public boolean accept(File file) {
      return file != null && file.isDirectory();
   }

   @Override
   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return this.toFileVisitResult(file != null && Files.isDirectory(file));
   }
}
