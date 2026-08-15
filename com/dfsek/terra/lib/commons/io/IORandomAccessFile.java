package com.dfsek.terra.lib.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Objects;

public final class IORandomAccessFile extends RandomAccessFile {
   private final File file;
   private final String mode;

   public IORandomAccessFile(File file, String mode) throws FileNotFoundException {
      super(file, mode);
      this.file = file;
      this.mode = mode;
   }

   public IORandomAccessFile(String name, String mode) throws FileNotFoundException {
      super(name, mode);
      this.file = name != null ? new File(name) : null;
      this.mode = mode;
   }

   public File getFile() {
      return this.file;
   }

   public String getMode() {
      return this.mode;
   }

   @Override
   public String toString() {
      return Objects.toString(this.file);
   }
}
