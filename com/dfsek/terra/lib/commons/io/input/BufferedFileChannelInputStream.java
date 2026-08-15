package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class BufferedFileChannelInputStream extends InputStream {
   private final ByteBuffer byteBuffer;
   private final FileChannel fileChannel;

   public static BufferedFileChannelInputStream.Builder builder() {
      return new BufferedFileChannelInputStream.Builder();
   }

   @Deprecated
   public BufferedFileChannelInputStream(File file) throws IOException {
      this(file, 8192);
   }

   @Deprecated
   public BufferedFileChannelInputStream(File file, int bufferSize) throws IOException {
      this(file.toPath(), bufferSize);
   }

   private BufferedFileChannelInputStream(FileChannel fileChannel, int bufferSize) {
      this.fileChannel = Objects.requireNonNull(fileChannel, "path");
      this.byteBuffer = ByteBuffer.allocateDirect(bufferSize);
      ((Buffer)this.byteBuffer).flip();
   }

   @Deprecated
   public BufferedFileChannelInputStream(Path path) throws IOException {
      this(path, 8192);
   }

   @Deprecated
   public BufferedFileChannelInputStream(Path path, int bufferSize) throws IOException {
      this(FileChannel.open(path, StandardOpenOption.READ), bufferSize);
   }

   @Override
   public synchronized int available() throws IOException {
      if (!this.fileChannel.isOpen()) {
         return 0;
      } else {
         return !this.refill() ? 0 : this.byteBuffer.remaining();
      }
   }

   private void clean(ByteBuffer buffer) {
      if (buffer.isDirect()) {
         this.cleanDirectBuffer(buffer);
      }
   }

   private void cleanDirectBuffer(ByteBuffer buffer) {
      if (ByteBufferCleaner.isSupported()) {
         ByteBufferCleaner.clean(buffer);
      }
   }

   @Override
   public synchronized void close() throws IOException {
      try {
         this.fileChannel.close();
      } finally {
         this.clean(this.byteBuffer);
      }
   }

   @Override
   public synchronized int read() throws IOException {
      return !this.refill() ? -1 : this.byteBuffer.get() & 0xFF;
   }

   @Override
   public synchronized int read(byte[] b, int offset, int len) throws IOException {
      if (offset < 0 || len < 0 || offset + len < 0 || offset + len > b.length) {
         throw new IndexOutOfBoundsException();
      }

      if (!this.refill()) {
         return -1;
      }

      len = Math.min(len, this.byteBuffer.remaining());
      this.byteBuffer.get(b, offset, len);
      return len;
   }

   private boolean refill() throws IOException {
      Input.checkOpen(this.fileChannel.isOpen());
      if (this.byteBuffer.hasRemaining()) {
         return true;
      }

      ((Buffer)this.byteBuffer).clear();
      int nRead = 0;

      while (nRead == 0) {
         nRead = this.fileChannel.read(this.byteBuffer);
      }

      ((Buffer)this.byteBuffer).flip();
      return nRead >= 0;
   }

   @Override
   public synchronized long skip(long n) throws IOException {
      if (n <= 0L) {
         return 0L;
      } else if (this.byteBuffer.remaining() >= n) {
         ((Buffer)this.byteBuffer).position(this.byteBuffer.position() + (int)n);
         return n;
      } else {
         long skippedFromBuffer = this.byteBuffer.remaining();
         long toSkipFromFileChannel = n - skippedFromBuffer;
         ((Buffer)this.byteBuffer).position(0);
         ((Buffer)this.byteBuffer).flip();
         return skippedFromBuffer + this.skipFromFileChannel(toSkipFromFileChannel);
      }
   }

   private long skipFromFileChannel(long n) throws IOException {
      long currentFilePosition = this.fileChannel.position();
      long size = this.fileChannel.size();
      if (n > size - currentFilePosition) {
         this.fileChannel.position(size);
         return size - currentFilePosition;
      } else {
         this.fileChannel.position(currentFilePosition + n);
         return n;
      }
   }

   public static class Builder extends AbstractStreamBuilder<BufferedFileChannelInputStream, BufferedFileChannelInputStream.Builder> {
      private FileChannel fileChannel;

      public BufferedFileChannelInputStream get() throws IOException {
         return this.fileChannel != null
            ? new BufferedFileChannelInputStream(this.fileChannel, this.getBufferSize())
            : new BufferedFileChannelInputStream(this.getPath(), this.getBufferSize());
      }

      public BufferedFileChannelInputStream.Builder setFileChannel(FileChannel fileChannel) {
         this.fileChannel = fileChannel;
         return this;
      }
   }
}
