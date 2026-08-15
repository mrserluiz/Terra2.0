package com.dfsek.terra.lib.commons.io.channels;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public final class FileChannels {
   @Deprecated
   public static boolean contentEquals(FileChannel channel1, FileChannel channel2, int bufferCapacity) throws IOException {
      return contentEquals((SeekableByteChannel)channel1, (SeekableByteChannel)channel2, bufferCapacity);
   }

   public static boolean contentEquals(ReadableByteChannel channel1, ReadableByteChannel channel2, int bufferCapacity) throws IOException {
      if (Objects.equals(channel1, channel2)) {
         return true;
      }

      ByteBuffer c1Buffer = ByteBuffer.allocateDirect(bufferCapacity);
      ByteBuffer c2Buffer = ByteBuffer.allocateDirect(bufferCapacity);
      int c1NumRead = 0;
      int c2NumRead = 0;
      boolean c1Read0 = false;
      boolean c2Read0 = false;

      while (true) {
         if (!c2Read0) {
            c1NumRead = readToLimit(channel1, c1Buffer);
            ((Buffer)c1Buffer).clear();
            c1Read0 = c1NumRead == 0;
         }

         if (!c1Read0) {
            c2NumRead = readToLimit(channel2, c2Buffer);
            ((Buffer)c2Buffer).clear();
            c2Read0 = c2NumRead == 0;
         }

         if (c1NumRead == -1 && c2NumRead == -1) {
            return c1Buffer.equals(c2Buffer);
         }

         if (c1NumRead != 0 && c2NumRead != 0) {
            if (c1NumRead != c2NumRead) {
               return false;
            }

            if (!c1Buffer.equals(c2Buffer)) {
               return false;
            }
         } else {
            Thread.yield();
         }
      }
   }

   public static boolean contentEquals(SeekableByteChannel channel1, SeekableByteChannel channel2, int bufferCapacity) throws IOException {
      if (Objects.equals(channel1, channel2)) {
         return true;
      }

      long size1 = size(channel1);
      long size2 = size(channel2);
      return size1 != size2 ? false : size1 == 0L && size2 == 0L || contentEquals((ReadableByteChannel)channel1, (ReadableByteChannel)channel2, bufferCapacity);
   }

   private static int readToLimit(ReadableByteChannel channel, ByteBuffer dst) throws IOException {
      if (!dst.hasRemaining()) {
         throw new IllegalArgumentException();
      }

      int totalRead = 0;

      int numRead;
      while (dst.hasRemaining() && (numRead = channel.read(dst)) != -1) {
         if (numRead == 0) {
            Thread.yield();
         } else {
            totalRead += numRead;
         }
      }

      return totalRead != 0 ? totalRead : -1;
   }

   private static long size(SeekableByteChannel channel) throws IOException {
      return channel != null ? channel.size() : 0L;
   }

   private FileChannels() {
   }
}
