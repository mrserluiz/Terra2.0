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

public class OrFileFilter extends AbstractFileFilter implements ConditionalFileFilter, Serializable {
   private static final long serialVersionUID = 5767770777065432721L;
   private final List<IOFileFilter> fileFilters;

   public OrFileFilter() {
      this(0);
   }

   private OrFileFilter(ArrayList<IOFileFilter> initialList) {
      this.fileFilters = Objects.requireNonNull(initialList, "initialList");
   }

   private OrFileFilter(int initialCapacity) {
      this(new ArrayList<>(initialCapacity));
   }

   public OrFileFilter(IOFileFilter... fileFilters) {
      this(Objects.requireNonNull(fileFilters, "fileFilters").length);
      this.addFileFilter(fileFilters);
   }

   public OrFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
      this(2);
      this.addFileFilter(filter1);
      this.addFileFilter(filter2);
   }

   public OrFileFilter(List<IOFileFilter> fileFilters) {
      this(new ArrayList<>(Objects.requireNonNull(fileFilters, "fileFilters")));
   }

   @Override
   public boolean accept(File file) {
      return this.fileFilters.stream().anyMatch(fileFilter -> fileFilter.accept(file));
   }

   @Override
   public boolean accept(File file, String name) {
      return this.fileFilters.stream().anyMatch(fileFilter -> fileFilter.accept(file, name));
   }

   @Override
   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return toDefaultFileVisitResult(this.fileFilters.stream().anyMatch(fileFilter -> fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE));
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

   @Override
   public boolean removeFileFilter(IOFileFilter fileFilter) {
      return this.fileFilters.remove(fileFilter);
   }

   @Override
   public void setFileFilters(List<IOFileFilter> fileFilters) {
      this.fileFilters.clear();
      this.fileFilters.addAll(Objects.requireNonNull(fileFilters, "fileFilters"));
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
