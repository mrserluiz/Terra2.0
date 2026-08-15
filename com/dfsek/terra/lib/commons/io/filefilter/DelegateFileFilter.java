package com.dfsek.terra.lib.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.Objects;

public class DelegateFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -8723373124984771318L;
   private final transient FileFilter fileFilter;
   private final transient FilenameFilter fileNameFilter;

   public DelegateFileFilter(FileFilter fileFilter) {
      Objects.requireNonNull(fileFilter, "filter");
      this.fileFilter = fileFilter;
      this.fileNameFilter = null;
   }

   public DelegateFileFilter(FilenameFilter fileNameFilter) {
      Objects.requireNonNull(fileNameFilter, "filter");
      this.fileNameFilter = fileNameFilter;
      this.fileFilter = null;
   }

   @Override
   public boolean accept(File file) {
      return this.fileFilter != null ? this.fileFilter.accept(file) : super.accept(file);
   }

   @Override
   public boolean accept(File dir, String name) {
      return this.fileNameFilter != null ? this.fileNameFilter.accept(dir, name) : super.accept(dir, name);
   }

   @Override
   public String toString() {
      String delegate = Objects.toString(this.fileFilter, Objects.toString(this.fileNameFilter, null));
      return super.toString() + "(" + delegate + ")";
   }
}
