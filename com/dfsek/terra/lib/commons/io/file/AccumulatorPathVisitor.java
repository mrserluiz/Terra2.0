package com.dfsek.terra.lib.commons.io.file;

import com.dfsek.terra.lib.commons.io.function.IOBiFunction;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AccumulatorPathVisitor extends CountingPathVisitor {
   private final List<Path> dirList = new ArrayList<>();
   private final List<Path> fileList = new ArrayList<>();

   public static AccumulatorPathVisitor.Builder builder() {
      return new AccumulatorPathVisitor.Builder();
   }

   public static AccumulatorPathVisitor withBigIntegerCounters() {
      return builder().setPathCounters(Counters.bigIntegerPathCounters()).get();
   }

   public static AccumulatorPathVisitor withBigIntegerCounters(PathFilter fileFilter, PathFilter dirFilter) {
      return builder().setPathCounters(Counters.bigIntegerPathCounters()).setFileFilter(fileFilter).setDirectoryFilter(dirFilter).get();
   }

   public static AccumulatorPathVisitor withLongCounters() {
      return builder().setPathCounters(Counters.longPathCounters()).get();
   }

   public static AccumulatorPathVisitor withLongCounters(PathFilter fileFilter, PathFilter dirFilter) {
      return builder().setPathCounters(Counters.longPathCounters()).setFileFilter(fileFilter).setDirectoryFilter(dirFilter).get();
   }

   @Deprecated
   public AccumulatorPathVisitor() {
      super(Counters.noopPathCounters());
   }

   private AccumulatorPathVisitor(AccumulatorPathVisitor.Builder builder) {
      super(builder);
   }

   @Deprecated
   public AccumulatorPathVisitor(Counters.PathCounters pathCounter) {
      super(pathCounter);
   }

   @Deprecated
   public AccumulatorPathVisitor(Counters.PathCounters pathCounter, PathFilter fileFilter, PathFilter dirFilter) {
      super(pathCounter, fileFilter, dirFilter);
   }

   @Deprecated
   public AccumulatorPathVisitor(
      Counters.PathCounters pathCounter, PathFilter fileFilter, PathFilter dirFilter, IOBiFunction<Path, IOException, FileVisitResult> visitFileFailed
   ) {
      super(pathCounter, fileFilter, dirFilter, visitFileFailed);
   }

   private void add(List<Path> list, Path dir) {
      list.add(dir.normalize());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!super.equals(obj)) {
         return false;
      }

      if (!(obj instanceof AccumulatorPathVisitor)) {
         return false;
      }

      AccumulatorPathVisitor other = (AccumulatorPathVisitor)obj;
      return Objects.equals(this.dirList, other.dirList) && Objects.equals(this.fileList, other.fileList);
   }

   public List<Path> getDirList() {
      return new ArrayList<>(this.dirList);
   }

   public List<Path> getFileList() {
      return new ArrayList<>(this.fileList);
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = super.hashCode();
      return 31 * result + Objects.hash(this.dirList, this.fileList);
   }

   public List<Path> relativizeDirectories(Path parent, boolean sort, Comparator<? super Path> comparator) {
      return PathUtils.relativize(this.getDirList(), parent, sort, comparator);
   }

   public List<Path> relativizeFiles(Path parent, boolean sort, Comparator<? super Path> comparator) {
      return PathUtils.relativize(this.getFileList(), parent, sort, comparator);
   }

   @Override
   protected void updateDirCounter(Path dir, IOException exc) {
      super.updateDirCounter(dir, exc);
      this.add(this.dirList, dir);
   }

   @Override
   protected void updateFileCounters(Path file, BasicFileAttributes attributes) {
      super.updateFileCounters(file, attributes);
      this.add(this.fileList, file);
   }

   public static class Builder extends CountingPathVisitor.AbstractBuilder<AccumulatorPathVisitor, AccumulatorPathVisitor.Builder> {
      public AccumulatorPathVisitor get() {
         return new AccumulatorPathVisitor(this);
      }
   }
}
