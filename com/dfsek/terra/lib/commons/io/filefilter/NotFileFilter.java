package com.dfsek.terra.lib.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class NotFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 6131563330944994230L;
   private final IOFileFilter filter;

   public NotFileFilter(IOFileFilter filter) {
      Objects.requireNonNull(filter, "filter");
      this.filter = filter;
   }

   @Override
   public boolean accept(File file) {
      return !this.filter.accept(file);
   }

   @Override
   public boolean accept(File file, String name) {
      return !this.filter.accept(file, name);
   }

   @Override
   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return this.not(this.filter.accept(file, attributes));
   }

   private FileVisitResult not(FileVisitResult accept) {
      return accept == FileVisitResult.CONTINUE ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
   }

   @Override
   public String toString() {
      return "NOT (" + this.filter.toString() + ")";
   }
}
