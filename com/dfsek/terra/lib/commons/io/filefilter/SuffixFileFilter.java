package com.dfsek.terra.lib.commons.io.filefilter;

import com.dfsek.terra.lib.commons.io.IOCase;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SuffixFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -3389157631240246157L;
   private final String[] suffixes;
   private final IOCase ioCase;

   public SuffixFileFilter(List<String> suffixes) {
      this(suffixes, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(List<String> suffixes, IOCase ioCase) {
      Objects.requireNonNull(suffixes, "suffixes");
      this.suffixes = suffixes.toArray(EMPTY_STRING_ARRAY);
      this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String suffix) {
      this(suffix, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String... suffixes) {
      this(suffixes, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String suffix, IOCase ioCase) {
      Objects.requireNonNull(suffix, "suffix");
      this.suffixes = new String[]{suffix};
      this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String[] suffixes, IOCase ioCase) {
      Objects.requireNonNull(suffixes, "suffixes");
      this.suffixes = (String[])suffixes.clone();
      this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   @Override
   public boolean accept(File file) {
      return this.accept(file.getName());
   }

   @Override
   public boolean accept(File file, String name) {
      return this.accept(name);
   }

   @Override
   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return this.toFileVisitResult(this.accept(PathUtils.getFileNameString(path)));
   }

   private boolean accept(String name) {
      return Stream.of(this.suffixes).anyMatch(suffix -> this.ioCase.checkEndsWith(name, suffix));
   }

   @Override
   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      this.append(this.suffixes, buffer);
      buffer.append(")");
      return buffer.toString();
   }
}
