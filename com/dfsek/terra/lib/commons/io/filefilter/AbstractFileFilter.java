package com.dfsek.terra.lib.commons.io.filefilter;

import com.dfsek.terra.lib.commons.io.file.PathVisitor;
import com.dfsek.terra.lib.commons.io.function.IOSupplier;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

public abstract class AbstractFileFilter implements IOFileFilter, PathVisitor {
   private final FileVisitResult onAccept;
   private final FileVisitResult onReject;

   static FileVisitResult toDefaultFileVisitResult(boolean accept) {
      return accept ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
   }

   public AbstractFileFilter() {
      this(FileVisitResult.CONTINUE, FileVisitResult.TERMINATE);
   }

   protected AbstractFileFilter(FileVisitResult onAccept, FileVisitResult onReject) {
      this.onAccept = onAccept;
      this.onReject = onReject;
   }

   @Override
   public boolean accept(File file) {
      Objects.requireNonNull(file, "file");
      return this.accept(file.getParentFile(), file.getName());
   }

   @Override
   public boolean accept(File dir, String name) {
      Objects.requireNonNull(name, "name");
      return this.accept(new File(dir, name));
   }

   void append(List<?> list, StringBuilder buffer) {
      for (int i = 0; i < list.size(); i++) {
         if (i > 0) {
            buffer.append(",");
         }

         buffer.append(list.get(i));
      }
   }

   void append(Object[] array, StringBuilder buffer) {
      for (int i = 0; i < array.length; i++) {
         if (i > 0) {
            buffer.append(",");
         }

         buffer.append(array[i]);
      }
   }

   FileVisitResult get(IOSupplier<FileVisitResult> supplier) {
      try {
         return supplier.get();
      } catch (IOException e) {
         return this.handle(e);
      }
   }

   protected FileVisitResult handle(Throwable t) {
      return FileVisitResult.TERMINATE;
   }

   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      return FileVisitResult.CONTINUE;
   }

   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
      return this.accept(dir, attributes);
   }

   FileVisitResult toFileVisitResult(boolean accept) {
      return accept ? this.onAccept : this.onReject;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName();
   }

   public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
      return this.accept(file, attributes);
   }

   public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      return FileVisitResult.CONTINUE;
   }
}
