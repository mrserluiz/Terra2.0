package com.dfsek.terra.lib.commons.io.filefilter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;

public class PathMatcherFileFilter extends AbstractFileFilter {
   private final PathMatcher pathMatcher;

   public PathMatcherFileFilter(PathMatcher pathMatcher) {
      this.pathMatcher = Objects.requireNonNull(pathMatcher, "pathMatcher");
   }

   @Override
   public boolean accept(File file) {
      return file != null && this.matches(file.toPath());
   }

   @Override
   public boolean matches(Path path) {
      return this.pathMatcher.matches(path);
   }
}
