package com.dfsek.terra.lib.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

final class ReverseFileComparator extends AbstractFileComparator implements Serializable {
   private static final long serialVersionUID = -4808255005272229056L;
   private final Comparator<File> delegate;

   ReverseFileComparator(Comparator<File> delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate");
   }

   public int compare(File file1, File file2) {
      return this.delegate.compare(file2, file1);
   }

   @Override
   public String toString() {
      return super.toString() + "[" + this.delegate.toString() + "]";
   }
}
