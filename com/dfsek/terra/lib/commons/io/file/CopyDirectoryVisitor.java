package com.dfsek.terra.lib.commons.io.file;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;

public class CopyDirectoryVisitor extends CountingPathVisitor {
   private final CopyOption[] copyOptions;
   private final Path sourceDirectory;
   private final Path targetDirectory;

   private static CopyOption[] toCopyOption(CopyOption... copyOptions) {
      return copyOptions == null ? PathUtils.EMPTY_COPY_OPTIONS : (CopyOption[])copyOptions.clone();
   }

   public CopyDirectoryVisitor(Counters.PathCounters pathCounter, Path sourceDirectory, Path targetDirectory, CopyOption... copyOptions) {
      super(pathCounter);
      this.sourceDirectory = sourceDirectory;
      this.targetDirectory = targetDirectory;
      this.copyOptions = toCopyOption(copyOptions);
   }

   public CopyDirectoryVisitor(
      Counters.PathCounters pathCounter, PathFilter fileFilter, PathFilter dirFilter, Path sourceDirectory, Path targetDirectory, CopyOption... copyOptions
   ) {
      super(pathCounter, fileFilter, dirFilter);
      this.sourceDirectory = sourceDirectory;
      this.targetDirectory = targetDirectory;
      this.copyOptions = toCopyOption(copyOptions);
   }

   protected void copy(Path sourceFile, Path targetFile) throws IOException {
      Files.copy(sourceFile, targetFile, this.copyOptions);
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

      CopyDirectoryVisitor other = (CopyDirectoryVisitor)obj;
      return Arrays.equals(this.copyOptions, other.copyOptions)
         && Objects.equals(this.sourceDirectory, other.sourceDirectory)
         && Objects.equals(this.targetDirectory, other.targetDirectory);
   }

   public CopyOption[] getCopyOptions() {
      return (CopyOption[])this.copyOptions.clone();
   }

   public Path getSourceDirectory() {
      return this.sourceDirectory;
   }

   public Path getTargetDirectory() {
      return this.targetDirectory;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = super.hashCode();
      result = 31 * result + Arrays.hashCode(this.copyOptions);
      return 31 * result + Objects.hash(this.sourceDirectory, this.targetDirectory);
   }

   @Override
   public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {
      Path newTargetDir = this.resolveRelativeAsString(directory);
      if (Files.notExists(newTargetDir)) {
         Files.createDirectory(newTargetDir);
      }

      return super.preVisitDirectory(directory, attributes);
   }

   private Path resolveRelativeAsString(Path directory) {
      return PathUtils.resolve(this.targetDirectory, this.sourceDirectory.relativize(directory));
   }

   @Override
   public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attributes) throws IOException {
      Path targetFile = this.resolveRelativeAsString(sourceFile);
      this.copy(sourceFile, targetFile);
      return super.visitFile(targetFile, attributes);
   }
}
