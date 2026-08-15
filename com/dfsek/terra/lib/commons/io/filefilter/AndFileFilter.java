package com.dfsek.terra.lib.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AndFileFilter extends AbstractFileFilter implements ConditionalFileFilter, Serializable {
   private static final long serialVersionUID = 7215974688563965257L;
   private final List<IOFileFilter> fileFilters;

   public AndFileFilter() {
      this(0);
   }

   private AndFileFilter(ArrayList<IOFileFilter> initialList) {
      this.fileFilters = Objects.requireNonNull(initialList, "initialList");
   }

   private AndFileFilter(int initialCapacity) {
      this(new ArrayList<>(initialCapacity));
   }

   public AndFileFilter(IOFileFilter... fileFilters) {
      this(Objects.requireNonNull(fileFilters, "fileFilters").length);
      this.addFileFilter(fileFilters);
   }

   public AndFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
      this(2);
      this.addFileFilter(filter1);
      this.addFileFilter(filter2);
   }

   public AndFileFilter(List<IOFileFilter> fileFilters) {
      this(new ArrayList<>(Objects.requireNonNull(fileFilters, "fileFilters")));
   }

   @Override
   public boolean accept(File file) {
      return !this.isEmpty() && this.fileFilters.stream().allMatch(fileFilter -> fileFilter.accept(file));
   }

   @Override
   public boolean accept(File file, String name) {
      return !this.isEmpty() && this.fileFilters.stream().allMatch(fileFilter -> fileFilter.accept(file, name));
   }

   @Override
   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return this.isEmpty()
         ? FileVisitResult.TERMINATE
         : toDefaultFileVisitResult(this.fileFilters.stream().allMatch(fileFilter -> fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE));
   }

   @Override
   public void addFileFilter(IOFileFilter fileFilter) {
      this.fileFilters.add(Objects.requireNonNull(fileFilter, "fileFilter"));
   }

   public void addFileFilter(IOFileFilter... fileFilters) {
      Stream.of(Objects.requireNonNull(fileFilters, "fileFilters")).forEach(this::addFileFilter);
   }

   @Override
   public List<IOFileFilter> getFileFilters() {
      return Collections.unmodifiableList(this.fileFilters);
   }

   private boolean isEmpty() {
      return this.fileFilters.isEmpty();
   }

   @Override
   public boolean removeFileFilter(IOFileFilter ioFileFilter) {
      return this.fileFilters.remove(ioFileFilter);
   }

   @Override
   public void setFileFilters(List<IOFileFilter> fileFilters) {
      this.fileFilters.clear();
      this.fileFilters.addAll(fileFilters);
   }

   @Override
   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      this.append(this.fileFilters, buffer);
      buffer.append(")");
      return buffer.toString();
   }
}
