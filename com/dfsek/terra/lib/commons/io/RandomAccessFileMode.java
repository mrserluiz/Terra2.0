package com.dfsek.terra.lib.commons.io;

import com.dfsek.terra.lib.commons.io.function.IOConsumer;
import com.dfsek.terra.lib.commons.io.function.IOFunction;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public enum RandomAccessFileMode {
   READ_ONLY("r", 1),
   READ_WRITE("rw", 2),
   READ_WRITE_SYNC_ALL("rws", 4),
   READ_WRITE_SYNC_CONTENT("rwd", 3);

   private static final String R = "r";
   private static final String RW = "rw";
   private static final String RWD = "rwd";
   private static final String RWS = "rws";
   private final int level;
   private final String mode;

   public static RandomAccessFileMode valueOf(OpenOption... openOption) {
      RandomAccessFileMode bestFit = READ_ONLY;

      for (OpenOption option : openOption) {
         if (option instanceof StandardOpenOption) {
            switch ((StandardOpenOption)option) {
               case WRITE:
                  if (!bestFit.implies(READ_WRITE)) {
                     bestFit = READ_WRITE;
                  }
                  break;
               case DSYNC:
                  if (!bestFit.implies(READ_WRITE_SYNC_CONTENT)) {
                     bestFit = READ_WRITE_SYNC_CONTENT;
                  }
                  break;
               case SYNC:
                  if (!bestFit.implies(READ_WRITE_SYNC_ALL)) {
                     bestFit = READ_WRITE_SYNC_ALL;
                  }
            }
         }
      }

      return bestFit;
   }

   public static RandomAccessFileMode valueOfMode(String mode) {
      switch (mode) {
         case "r":
            return READ_ONLY;
         case "rw":
            return READ_WRITE;
         case "rwd":
            return READ_WRITE_SYNC_CONTENT;
         case "rws":
            return READ_WRITE_SYNC_ALL;
         default:
            throw new IllegalArgumentException(mode);
      }
   }

   RandomAccessFileMode(final String mode, final int level) {
      this.mode = mode;
      this.level = level;
   }

   public void accept(Path file, IOConsumer<RandomAccessFile> consumer) throws IOException {
      RandomAccessFile raf = this.create(file);

      try {
         consumer.accept(raf);
      } catch (Throwable var7) {
         if (raf != null) {
            try {
               raf.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (raf != null) {
         raf.close();
      }
   }

   public <T> T apply(Path file, IOFunction<RandomAccessFile, T> function) throws IOException {
      RandomAccessFile raf = this.create(file);

      Object var4;
      try {
         var4 = function.apply(raf);
      } catch (Throwable var7) {
         if (raf != null) {
            try {
               raf.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (raf != null) {
         raf.close();
      }

      return (T)var4;
   }

   public RandomAccessFile create(File file) throws FileNotFoundException {
      return new IORandomAccessFile(file, this.mode);
   }

   public RandomAccessFile create(Path file) throws FileNotFoundException {
      return this.create(Objects.requireNonNull(file.toFile(), "file"));
   }

   public RandomAccessFile create(String name) throws FileNotFoundException {
      return new IORandomAccessFile(name, this.mode);
   }

   private int getLevel() {
      return this.level;
   }

   public String getMode() {
      return this.mode;
   }

   public boolean implies(RandomAccessFileMode other) {
      return this.getLevel() >= other.getLevel();
   }

   public IORandomAccessFile io(String name) throws FileNotFoundException {
      return new IORandomAccessFile(name, this.mode);
   }
}
