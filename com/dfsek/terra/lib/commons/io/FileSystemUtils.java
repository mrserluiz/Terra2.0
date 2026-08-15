package com.dfsek.terra.lib.commons.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Deprecated
public class FileSystemUtils {
   @Deprecated
   public static long freeSpace(String path) throws IOException {
      return getFreeSpace(path);
   }

   @Deprecated
   public static long freeSpaceKb() throws IOException {
      return freeSpaceKb(-1L);
   }

   @Deprecated
   public static long freeSpaceKb(long timeout) throws IOException {
      return freeSpaceKb(FileUtils.current().getAbsolutePath(), timeout);
   }

   @Deprecated
   public static long freeSpaceKb(String path) throws IOException {
      return freeSpaceKb(path, -1L);
   }

   @Deprecated
   public static long freeSpaceKb(String path, long timeout) throws IOException {
      return getFreeSpace(path) / 1024L;
   }

   static long getFreeSpace(String pathStr) throws IOException {
      Path path = Paths.get(Objects.requireNonNull(pathStr, "pathStr"));
      if (Files.exists(path)) {
         return Files.getFileStore(path.toAbsolutePath()).getUsableSpace();
      } else {
         throw new IllegalArgumentException(path.toString());
      }
   }
}
