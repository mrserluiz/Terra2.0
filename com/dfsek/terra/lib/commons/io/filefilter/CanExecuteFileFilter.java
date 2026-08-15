package com.dfsek.terra.lib.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class CanExecuteFileFilter extends AbstractFileFilter implements Serializable {
   public static final IOFileFilter CAN_EXECUTE = new CanExecuteFileFilter();
   public static final IOFileFilter CANNOT_EXECUTE = CAN_EXECUTE.negate();
   private static final long serialVersionUID = 3179904805251622989L;

   protected CanExecuteFileFilter() {
   }

   @Override
   public boolean accept(File file) {
      return file != null && file.canExecute();
   }

   @Override
   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return this.toFileVisitResult(file != null && Files.isExecutable(file));
   }
}
