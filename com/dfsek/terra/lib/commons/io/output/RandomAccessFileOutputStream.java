package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.StandardOpenOption;

public final class RandomAccessFileOutputStream extends OutputStream {
   private final RandomAccessFile randomAccessFile;

   public static RandomAccessFileOutputStream.Builder builder() {
      return new RandomAccessFileOutputStream.Builder();
   }

   private RandomAccessFileOutputStream(RandomAccessFileOutputStream.Builder builder) throws IOException {
      this.randomAccessFile = builder.getRandomAccessFile();
   }

   @Override
   public void close() throws IOException {
      this.randomAccessFile.close();
      super.close();
   }

   @Override
   public void flush() throws IOException {
      this.randomAccessFile.getChannel().force(true);
      super.flush();
   }

   public RandomAccessFile getRandomAccessFile() {
      return this.randomAccessFile;
   }

   @Override
   public void write(int b) throws IOException {
      this.randomAccessFile.write(b);
   }

   public static final class Builder extends AbstractStreamBuilder<RandomAccessFileOutputStream, RandomAccessFileOutputStream.Builder> {
      private Builder() {
         this.setOpenOptions(StandardOpenOption.WRITE);
      }

      public RandomAccessFileOutputStream get() throws IOException {
         return new RandomAccessFileOutputStream(this);
      }
   }
}
