package com.dfsek.terra.lib.commons.io.filefilter;

import com.dfsek.terra.lib.commons.io.file.PathFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

public interface IOFileFilter extends FileFilter, FilenameFilter, PathFilter, PathMatcher {
   String[] EMPTY_STRING_ARRAY = new String[0];

   @Override
   boolean accept(File var1);

   @Override
   boolean accept(File var1, String var2);

   @Override
   default FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return AbstractFileFilter.toDefaultFileVisitResult(path != null && this.accept(path.toFile()));
   }

   default IOFileFilter and(IOFileFilter fileFilter) {
      return new AndFileFilter(this, fileFilter);
   }

   @Override
   default boolean matches(Path path) {
      return this.accept(path, null) != FileVisitResult.TERMINATE;
   }

   default IOFileFilter negate() {
      return new NotFileFilter(this);
   }

   default IOFileFilter or(IOFileFilter fileFilter) {
      return new OrFileFilter(this, fileFilter);
   }
}
