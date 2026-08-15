package com.dfsek.terra.lib.commons.io;

import com.dfsek.terra.lib.commons.io.channels.FileChannels;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public class RandomAccessFiles {
   public static boolean contentEquals(RandomAccessFile raf1, RandomAccessFile raf2) throws IOException {
      if (Objects.equals(raf1, raf2)) {
         return true;
      } else {
         long length1 = length(raf1);
         long length2 = length(raf2);
         if (length1 != length2) {
            return false;
         } else {
            return length1 == 0L && length2 == 0L
               ? true
               : FileChannels.contentEquals((SeekableByteChannel)raf1.getChannel(), (SeekableByteChannel)raf2.getChannel(), 8192);
         }
      }
   }

   private static long length(RandomAccessFile raf) throws IOException {
      return raf != null ? raf.length() : 0L;
   }

   public static byte[] read(RandomAccessFile input, long position, int length) throws IOException {
      input.seek(position);
      return IOUtils.toByteArray(input::read, length);
   }

   public static RandomAccessFile reset(RandomAccessFile raf) throws IOException {
      raf.seek(0L);
      return raf;
   }
}
