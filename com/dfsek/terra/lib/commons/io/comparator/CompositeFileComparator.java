package com.dfsek.terra.lib.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CompositeFileComparator extends AbstractFileComparator implements Serializable {
   private static final Comparator<?>[] EMPTY_COMPARATOR_ARRAY = new Comparator[0];
   private static final long serialVersionUID = -2224170307287243428L;
   private final Comparator<File>[] delegates;

   public CompositeFileComparator(Comparator<File>... delegates) {
      this.delegates = delegates == null ? this.emptyArray() : (Comparator[])delegates.clone();
   }

   public CompositeFileComparator(Iterable<Comparator<File>> delegates) {
      this.delegates = delegates == null ? this.emptyArray() : StreamSupport.stream(delegates.spliterator(), false).toArray(Comparator[]::new);
   }

   public int compare(File file1, File file2) {
      return Stream.of(this.delegates).map(delegate -> delegate.compare(file1, file2)).filter(r -> r != 0).findFirst().orElse(0);
   }

   private Comparator<File>[] emptyArray() {
      return (Comparator<File>[])EMPTY_COMPARATOR_ARRAY;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(super.toString());
      builder.append('{');

      for (int i = 0; i < this.delegates.length; i++) {
         if (i > 0) {
            builder.append(',');
         }

         builder.append(this.delegates[i]);
      }

      builder.append('}');
      return builder.toString();
   }
}
