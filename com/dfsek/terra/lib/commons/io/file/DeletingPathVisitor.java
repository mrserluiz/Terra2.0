package com.dfsek.terra.lib.commons.io.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;

public class DeletingPathVisitor extends CountingPathVisitor {
   private final String[] skip;
   private final boolean overrideReadOnly;
   private final LinkOption[] linkOptions;

   public static DeletingPathVisitor withBigIntegerCounters() {
      return new DeletingPathVisitor(Counters.bigIntegerPathCounters());
   }

   public static DeletingPathVisitor withLongCounters() {
      return new DeletingPathVisitor(Counters.longPathCounters());
   }

   public DeletingPathVisitor(Counters.PathCounters pathCounter, DeleteOption[] deleteOption, String... skip) {
      this(pathCounter, PathUtils.noFollowLinkOptionArray(), deleteOption, skip);
   }

   public DeletingPathVisitor(Counters.PathCounters pathCounter, LinkOption[] linkOptions, DeleteOption[] deleteOption, String... skip) {
      super(pathCounter);
      String[] temp = skip != null ? (String[])skip.clone() : EMPTY_STRING_ARRAY;
      Arrays.sort(temp);
      this.skip = temp;
      this.overrideReadOnly = StandardDeleteOption.overrideReadOnly(deleteOption);
      this.linkOptions = linkOptions == null ? PathUtils.noFollowLinkOptionArray() : (LinkOption[])linkOptions.clone();
   }

   public DeletingPathVisitor(Counters.PathCounters pathCounter, String... skip) {
      this(pathCounter, PathUtils.EMPTY_DELETE_OPTION_ARRAY, skip);
   }

   private boolean accept(Path path) {
      return Arrays.binarySearch(this.skip, PathUtils.getFileNameString(path)) < 0;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!super.equals(obj)) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      DeletingPathVisitor other = (DeletingPathVisitor)obj;
      return this.overrideReadOnly == other.overrideReadOnly && Arrays.equals(this.skip, other.skip);
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = super.hashCode();
      result = 31 * result + Arrays.hashCode(this.skip);
      return 31 * result + Objects.hash(this.overrideReadOnly);
   }

   @Override
   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      if (PathUtils.isEmptyDirectory(dir)) {
         Files.deleteIfExists(dir);
      }

      return super.postVisitDirectory(dir, exc);
   }

   @Override
   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      super.preVisitDirectory(dir, attrs);
      return this.accept(dir) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
   }

   @Override
   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      if (this.accept(file)) {
         if (Files.exists(file, this.linkOptions)) {
            if (this.overrideReadOnly) {
               PathUtils.setReadOnly(file, false, this.linkOptions);
            }

            Files.deleteIfExists(file);
         }

         if (Files.isSymbolicLink(file)) {
            try {
               Files.delete(file);
            } catch (NoSuchFileException var4) {
            }
         }
      }

      this.updateFileCounters(file, attrs);
      return FileVisitResult.CONTINUE;
   }
}
