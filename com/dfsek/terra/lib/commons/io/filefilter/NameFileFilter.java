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

public class NameFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 176844364689077340L;
   private final String[] names;
   private final IOCase ioCase;

   public NameFileFilter(List<String> names) {
      this(names, null);
   }

   public NameFileFilter(List<String> names, IOCase ioCase) {
      Objects.requireNonNull(names, "names");
      this.names = names.toArray(EMPTY_STRING_ARRAY);
      this.ioCase = this.toIOCase(ioCase);
   }

   public NameFileFilter(String name) {
      this(name, IOCase.SENSITIVE);
   }

   public NameFileFilter(String... names) {
      this(names, IOCase.SENSITIVE);
   }

   public NameFileFilter(String name, IOCase ioCase) {
      Objects.requireNonNull(name, "name");
      this.names = new String[]{name};
      this.ioCase = this.toIOCase(ioCase);
   }

   public NameFileFilter(String[] names, IOCase ioCase) {
      Objects.requireNonNull(names, "names");
      this.names = (String[])names.clone();
      this.ioCase = this.toIOCase(ioCase);
   }

   @Override
   public boolean accept(File file) {
      return file != null && this.acceptBaseName(file.getName());
   }

   @Override
   public boolean accept(File dir, String name) {
      return this.acceptBaseName(name);
   }

   @Override
   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return this.toFileVisitResult(this.acceptBaseName(PathUtils.getFileNameString(path)));
   }

   private boolean acceptBaseName(String baseName) {
      return Stream.of(this.names).anyMatch(testName -> this.ioCase.checkEquals(baseName, testName));
   }

   private IOCase toIOCase(IOCase ioCase) {
      return IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   @Override
   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      this.append(this.names, buffer);
      buffer.append(")");
      return buffer.toString();
   }
}
