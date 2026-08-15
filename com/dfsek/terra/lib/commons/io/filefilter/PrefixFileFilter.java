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

public class PrefixFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 8533897440809599867L;
   private final String[] prefixes;
   private final IOCase isCase;

   public PrefixFileFilter(List<String> prefixes) {
      this(prefixes, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(List<String> prefixes, IOCase ioCase) {
      Objects.requireNonNull(prefixes, "prefixes");
      this.prefixes = prefixes.toArray(EMPTY_STRING_ARRAY);
      this.isCase = IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String prefix) {
      this(prefix, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String... prefixes) {
      this(prefixes, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String prefix, IOCase ioCase) {
      Objects.requireNonNull(prefix, "prefix");
      this.prefixes = new String[]{prefix};
      this.isCase = IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String[] prefixes, IOCase ioCase) {
      Objects.requireNonNull(prefixes, "prefixes");
      this.prefixes = (String[])prefixes.clone();
      this.isCase = IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   @Override
   public boolean accept(File file) {
      return this.accept(file == null ? null : file.getName());
   }

   @Override
   public boolean accept(File file, String name) {
      return this.accept(name);
   }

   @Override
   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return this.toFileVisitResult(this.accept(PathUtils.getFileName(file, Path::toFile)));
   }

   private boolean accept(String name) {
      return Stream.of(this.prefixes).anyMatch(prefix -> this.isCase.checkStartsWith(name, prefix));
   }

   @Override
   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      this.append(this.prefixes, buffer);
      buffer.append(")");
      return buffer.toString();
   }
}
