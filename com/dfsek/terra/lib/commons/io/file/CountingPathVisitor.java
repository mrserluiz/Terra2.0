package com.dfsek.terra.lib.commons.io.file;

import com.dfsek.terra.lib.commons.io.filefilter.IOFileFilter;
import com.dfsek.terra.lib.commons.io.filefilter.SymbolicLinkFileFilter;
import com.dfsek.terra.lib.commons.io.filefilter.TrueFileFilter;
import com.dfsek.terra.lib.commons.io.function.IOBiFunction;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class CountingPathVisitor extends SimplePathVisitor {
   static final String[] EMPTY_STRING_ARRAY = new String[0];
   private final Counters.PathCounters pathCounters;
   private final PathFilter fileFilter;
   private final PathFilter directoryFilter;
   private final UnaryOperator<Path> directoryPostTransformer;

   static IOFileFilter defaultDirectoryFilter() {
      return TrueFileFilter.INSTANCE;
   }

   static UnaryOperator<Path> defaultDirectoryTransformer() {
      return UnaryOperator.identity();
   }

   static IOFileFilter defaultFileFilter() {
      return new SymbolicLinkFileFilter(FileVisitResult.TERMINATE, FileVisitResult.CONTINUE);
   }

   static Counters.PathCounters defaultPathCounters() {
      return Counters.longPathCounters();
   }

   public static CountingPathVisitor withBigIntegerCounters() {
      return new CountingPathVisitor.Builder().setPathCounters(Counters.bigIntegerPathCounters()).get();
   }

   public static CountingPathVisitor withLongCounters() {
      return new CountingPathVisitor.Builder().setPathCounters(Counters.longPathCounters()).get();
   }

   CountingPathVisitor(CountingPathVisitor.AbstractBuilder<?, ?> builder) {
      super(builder);
      this.pathCounters = builder.getPathCounters();
      this.fileFilter = builder.getFileFilter();
      this.directoryFilter = builder.getDirectoryFilter();
      this.directoryPostTransformer = builder.getDirectoryPostTransformer();
   }

   public CountingPathVisitor(Counters.PathCounters pathCounters) {
      this(new CountingPathVisitor.Builder().setPathCounters(pathCounters));
   }

   public CountingPathVisitor(Counters.PathCounters pathCounters, PathFilter fileFilter, PathFilter directoryFilter) {
      this.pathCounters = Objects.requireNonNull(pathCounters, "pathCounters");
      this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
      this.directoryFilter = Objects.requireNonNull(directoryFilter, "directoryFilter");
      this.directoryPostTransformer = UnaryOperator.identity();
   }

   @Deprecated
   public CountingPathVisitor(
      Counters.PathCounters pathCounters, PathFilter fileFilter, PathFilter directoryFilter, IOBiFunction<Path, IOException, FileVisitResult> visitFileFailed
   ) {
      super(visitFileFailed);
      this.pathCounters = Objects.requireNonNull(pathCounters, "pathCounters");
      this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
      this.directoryFilter = Objects.requireNonNull(directoryFilter, "directoryFilter");
      this.directoryPostTransformer = UnaryOperator.identity();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof CountingPathVisitor)) {
         return false;
      }

      CountingPathVisitor other = (CountingPathVisitor)obj;
      return Objects.equals(this.pathCounters, other.pathCounters);
   }

   public Counters.PathCounters getPathCounters() {
      return this.pathCounters;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.pathCounters);
   }

   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      this.updateDirCounter(this.directoryPostTransformer.apply(dir), exc);
      return FileVisitResult.CONTINUE;
   }

   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
      FileVisitResult accept = this.directoryFilter.accept(dir, attributes);
      return accept != FileVisitResult.CONTINUE ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
   }

   @Override
   public String toString() {
      return this.pathCounters.toString();
   }

   protected void updateDirCounter(Path dir, IOException exc) {
      this.pathCounters.getDirectoryCounter().increment();
   }

   protected void updateFileCounters(Path file, BasicFileAttributes attributes) {
      this.pathCounters.getFileCounter().increment();
      this.pathCounters.getByteCounter().add(attributes.size());
   }

   public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
      if (Files.exists(file) && this.fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE) {
         this.updateFileCounters(file, attributes);
      }

      return FileVisitResult.CONTINUE;
   }

   public abstract static class AbstractBuilder<T, B extends CountingPathVisitor.AbstractBuilder<T, B>> extends SimplePathVisitor.AbstractBuilder<T, B> {
      private Counters.PathCounters pathCounters = CountingPathVisitor.defaultPathCounters();
      private PathFilter fileFilter = CountingPathVisitor.defaultFileFilter();
      private PathFilter directoryFilter = CountingPathVisitor.defaultDirectoryFilter();
      private UnaryOperator<Path> directoryPostTransformer = CountingPathVisitor.defaultDirectoryTransformer();

      PathFilter getDirectoryFilter() {
         return this.directoryFilter;
      }

      UnaryOperator<Path> getDirectoryPostTransformer() {
         return this.directoryPostTransformer;
      }

      PathFilter getFileFilter() {
         return this.fileFilter;
      }

      Counters.PathCounters getPathCounters() {
         return this.pathCounters;
      }

      public B setDirectoryFilter(PathFilter directoryFilter) {
         this.directoryFilter = directoryFilter != null ? directoryFilter : CountingPathVisitor.defaultDirectoryFilter();
         return this.asThis();
      }

      public B setDirectoryPostTransformer(UnaryOperator<Path> directoryTransformer) {
         this.directoryPostTransformer = directoryTransformer != null ? directoryTransformer : CountingPathVisitor.defaultDirectoryTransformer();
         return this.asThis();
      }

      public B setFileFilter(PathFilter fileFilter) {
         this.fileFilter = fileFilter != null ? fileFilter : CountingPathVisitor.defaultFileFilter();
         return this.asThis();
      }

      public B setPathCounters(Counters.PathCounters pathCounters) {
         this.pathCounters = pathCounters != null ? pathCounters : CountingPathVisitor.defaultPathCounters();
         return this.asThis();
      }
   }

   public static class Builder extends CountingPathVisitor.AbstractBuilder<CountingPathVisitor, CountingPathVisitor.Builder> {
      public CountingPathVisitor get() {
         return new CountingPathVisitor(this);
      }
   }
}
