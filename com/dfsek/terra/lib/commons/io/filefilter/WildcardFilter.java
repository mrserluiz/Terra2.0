package com.dfsek.terra.lib.commons.io.filefilter;

import com.dfsek.terra.lib.commons.io.FilenameUtils;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Deprecated
public class WildcardFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -5037645902506953517L;
   private final String[] wildcards;

   public WildcardFilter(List<String> wildcards) {
      Objects.requireNonNull(wildcards, "wildcards");
      this.wildcards = wildcards.toArray(EMPTY_STRING_ARRAY);
   }

   public WildcardFilter(String wildcard) {
      Objects.requireNonNull(wildcard, "wildcard");
      this.wildcards = new String[]{wildcard};
   }

   public WildcardFilter(String... wildcards) {
      Objects.requireNonNull(wildcards, "wildcards");
      this.wildcards = (String[])wildcards.clone();
   }

   @Override
   public boolean accept(File file) {
      return file.isDirectory() ? false : Stream.of(this.wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(file.getName(), wildcard));
   }

   @Override
   public boolean accept(File dir, String name) {
      return dir != null && new File(dir, name).isDirectory()
         ? false
         : Stream.of(this.wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(name, wildcard));
   }

   @Override
   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return Files.isDirectory(path)
         ? FileVisitResult.TERMINATE
         : toDefaultFileVisitResult(Stream.of(this.wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(PathUtils.getFileNameString(path), wildcard)));
   }
}
