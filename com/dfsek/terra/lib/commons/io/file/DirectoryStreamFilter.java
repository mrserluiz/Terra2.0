package com.dfsek.terra.lib.commons.io.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.DirectoryStream.Filter;
import java.util.Objects;

public class DirectoryStreamFilter implements Filter<Path> {
   private final PathFilter pathFilter;

   public DirectoryStreamFilter(PathFilter pathFilter) {
      this.pathFilter = Objects.requireNonNull(pathFilter, "pathFilter");
   }

   public boolean accept(Path path) throws IOException {
      return this.pathFilter.accept(path, PathUtils.readBasicFileAttributes(path, PathUtils.EMPTY_LINK_OPTION_ARRAY)) == FileVisitResult.CONTINUE;
   }

   public PathFilter getPathFilter() {
      return this.pathFilter;
   }
}
