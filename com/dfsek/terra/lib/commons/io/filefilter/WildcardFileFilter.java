package com.dfsek.terra.lib.commons.io.filefilter;

import com.dfsek.terra.lib.commons.io.FilenameUtils;
import com.dfsek.terra.lib.commons.io.IOCase;
import com.dfsek.terra.lib.commons.io.build.AbstractSupplier;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class WildcardFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -7426486598995782105L;
   private final String[] wildcards;
   private final IOCase ioCase;

   public static WildcardFileFilter.Builder builder() {
      return new WildcardFileFilter.Builder();
   }

   private static <T> T requireWildcards(T wildcards) {
      return Objects.requireNonNull(wildcards, "wildcards");
   }

   private WildcardFileFilter(IOCase ioCase, String... wildcards) {
      this.wildcards = (String[])requireWildcards(wildcards).clone();
      this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
   }

   @Deprecated
   public WildcardFileFilter(List<String> wildcards) {
      this(wildcards, IOCase.SENSITIVE);
   }

   @Deprecated
   public WildcardFileFilter(List<String> wildcards, IOCase ioCase) {
      this(ioCase, requireWildcards(wildcards).toArray(EMPTY_STRING_ARRAY));
   }

   @Deprecated
   public WildcardFileFilter(String wildcard) {
      this(IOCase.SENSITIVE, requireWildcards(wildcard));
   }

   @Deprecated
   public WildcardFileFilter(String... wildcards) {
      this(IOCase.SENSITIVE, wildcards);
   }

   @Deprecated
   public WildcardFileFilter(String wildcard, IOCase ioCase) {
      this(ioCase, wildcard);
   }

   @Deprecated
   public WildcardFileFilter(String[] wildcards, IOCase ioCase) {
      this(ioCase, wildcards);
   }

   @Override
   public boolean accept(File file) {
      return this.accept(file.getName());
   }

   @Override
   public boolean accept(File dir, String name) {
      return this.accept(name);
   }

   @Override
   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return this.toFileVisitResult(this.accept(PathUtils.getFileNameString(path)));
   }

   private boolean accept(String name) {
      return Stream.of(this.wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(name, wildcard, this.ioCase));
   }

   @Override
   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      this.append(this.wildcards, buffer);
      buffer.append(")");
      return buffer.toString();
   }

   public static class Builder extends AbstractSupplier<WildcardFileFilter, WildcardFileFilter.Builder> {
      private String[] wildcards;
      private IOCase ioCase = IOCase.SENSITIVE;

      public WildcardFileFilter get() {
         return new WildcardFileFilter(this.ioCase, this.wildcards);
      }

      public WildcardFileFilter.Builder setIoCase(IOCase ioCase) {
         this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
         return this;
      }

      public WildcardFileFilter.Builder setWildcards(List<String> wildcards) {
         this.setWildcards(WildcardFileFilter.requireWildcards(wildcards).toArray(IOFileFilter.EMPTY_STRING_ARRAY));
         return this;
      }

      public WildcardFileFilter.Builder setWildcards(String... wildcards) {
         this.wildcards = WildcardFileFilter.requireWildcards(wildcards);
         return this;
      }
   }
}
