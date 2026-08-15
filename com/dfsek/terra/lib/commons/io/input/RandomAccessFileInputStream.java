package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.IOUtils;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

public class RandomAccessFileInputStream extends AbstractInputStream {
   private final boolean propagateClose;
   private final RandomAccessFile randomAccessFile;

   public static RandomAccessFileInputStream.Builder builder() {
      return new RandomAccessFileInputStream.Builder();
   }

   @Deprecated
   public RandomAccessFileInputStream(RandomAccessFile file) {
      this(file, false);
   }

   @Deprecated
   public RandomAccessFileInputStream(RandomAccessFile file, boolean propagateClose) {
      this.randomAccessFile = Objects.requireNonNull(file, "file");
      this.propagateClose = propagateClose;
   }

   @Override
   public int available() throws IOException {
      return Math.toIntExact(Math.min(this.availableLong(), 2147483647L));
   }

   public long availableLong() throws IOException {
      return this.isClosed() ? 0L : this.randomAccessFile.length() - this.randomAccessFile.getFilePointer();
   }

   @Override
   public void close() throws IOException {
      super.close();
      if (this.propagateClose) {
         this.randomAccessFile.close();
      }
   }

   public long copy(long pos, long size, OutputStream os) throws IOException {
      this.randomAccessFile.seek(pos);
      return IOUtils.copyLarge(this, os, 0L, size);
   }

   public RandomAccessFile getRandomAccessFile() {
      return this.randomAccessFile;
   }

   public boolean isCloseOnClose() {
      return this.propagateClose;
   }

   @Override
   public int read() throws IOException {
      return this.randomAccessFile.read();
   }

   @Override
   public int read(byte[] bytes) throws IOException {
      return this.randomAccessFile.read(bytes);
   }

   @Override
   public int read(byte[] bytes, int offset, int length) throws IOException {
      return this.randomAccessFile.read(bytes, offset, length);
   }

   @Override
   public long skip(long skipCount) throws IOException {
      if (skipCount <= 0L) {
         return 0L;
      }

      long filePointer = this.randomAccessFile.getFilePointer();
      long fileLength = this.randomAccessFile.length();
      if (filePointer >= fileLength) {
         return 0L;
      }

      long targetPos = filePointer + skipCount;
      long newPos = targetPos > fileLength ? fileLength - 1L : targetPos;
      if (newPos > 0L) {
         this.randomAccessFile.seek(newPos);
      }

      return this.randomAccessFile.getFilePointer() - filePointer;
   }

   public static class Builder extends AbstractStreamBuilder<RandomAccessFileInputStream, RandomAccessFileInputStream.Builder> {
      private boolean propagateClose;

      public RandomAccessFileInputStream get() throws IOException {
         return new RandomAccessFileInputStream(this.getRandomAccessFile(), this.propagateClose);
      }

      public RandomAccessFileInputStream.Builder setCloseOnClose(boolean propagateClose) {
         this.propagateClose = propagateClose;
         return this;
      }

      public RandomAccessFileInputStream.Builder setRandomAccessFile(RandomAccessFile randomAccessFile) {
         return (RandomAccessFileInputStream.Builder)super.setRandomAccessFile(randomAccessFile);
      }
   }
}
