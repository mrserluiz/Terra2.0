package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.function.IOBiConsumer;
import java.io.IOException;
import java.io.InputStream;

public class BoundedInputStream extends ProxyInputStream {
   private long count;
   private long mark;
   private final long maxCount;
   private final IOBiConsumer<Long, Long> onMaxCount;
   private boolean propagateClose = true;

   public static BoundedInputStream.Builder builder() {
      return new BoundedInputStream.Builder();
   }

   BoundedInputStream(BoundedInputStream.Builder builder) throws IOException {
      super(builder);
      this.count = builder.getCount();
      this.maxCount = builder.getMaxCount();
      this.propagateClose = builder.isPropagateClose();
      this.onMaxCount = builder.getOnMaxCount();
   }

   @Deprecated
   public BoundedInputStream(InputStream in) {
      this(in, -1L);
   }

   BoundedInputStream(InputStream inputStream, BoundedInputStream.Builder builder) {
      super(inputStream, builder);
      this.count = builder.getCount();
      this.maxCount = builder.getMaxCount();
      this.propagateClose = builder.isPropagateClose();
      this.onMaxCount = builder.getOnMaxCount();
   }

   @Deprecated
   public BoundedInputStream(InputStream inputStream, long maxCount) {
      this(inputStream, (BoundedInputStream.Builder)builder().setMaxCount(maxCount));
   }

   @Override
   protected synchronized void afterRead(int n) throws IOException {
      if (n != -1) {
         this.count += n;
      }

      super.afterRead(n);
   }

   @Override
   public int available() throws IOException {
      if (this.isMaxCount()) {
         this.onMaxLength(this.maxCount, this.getCount());
         return 0;
      } else {
         return this.in.available();
      }
   }

   @Override
   public void close() throws IOException {
      if (this.propagateClose) {
         super.close();
      }
   }

   public synchronized long getCount() {
      return this.count;
   }

   public long getMaxCount() {
      return this.maxCount;
   }

   @Deprecated
   public long getMaxLength() {
      return this.maxCount;
   }

   public long getRemaining() {
      return Math.max(0L, this.getMaxCount() - this.getCount());
   }

   private boolean isMaxCount() {
      return this.maxCount >= 0L && this.getCount() >= this.maxCount;
   }

   public boolean isPropagateClose() {
      return this.propagateClose;
   }

   @Override
   public synchronized void mark(int readLimit) {
      this.in.mark(readLimit);
      this.mark = this.count;
   }

   @Override
   public boolean markSupported() {
      return this.in.markSupported();
   }

   protected void onMaxLength(long max, long count) throws IOException {
      this.onMaxCount.accept(max, count);
   }

   @Override
   public int read() throws IOException {
      if (this.isMaxCount()) {
         this.onMaxLength(this.maxCount, this.getCount());
         return -1;
      } else {
         return super.read();
      }
   }

   @Override
   public int read(byte[] b) throws IOException {
      return this.read(b, 0, b.length);
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      if (this.isMaxCount()) {
         this.onMaxLength(this.maxCount, this.getCount());
         return -1;
      } else {
         return super.read(b, off, (int)this.toReadLen(len));
      }
   }

   @Override
   public synchronized void reset() throws IOException {
      this.in.reset();
      this.count = this.mark;
   }

   @Deprecated
   public void setPropagateClose(boolean propagateClose) {
      this.propagateClose = propagateClose;
   }

   @Override
   public synchronized long skip(long n) throws IOException {
      long skip = super.skip(this.toReadLen(n));
      this.count += skip;
      return skip;
   }

   private long toReadLen(long len) {
      return this.maxCount >= 0L ? Math.min(len, this.maxCount - this.getCount()) : len;
   }

   @Override
   public String toString() {
      return this.in.toString();
   }

   abstract static class AbstractBuilder<T extends BoundedInputStream.AbstractBuilder<T>> extends ProxyInputStream.AbstractBuilder<BoundedInputStream, T> {
      private long count;
      private long maxCount = -1L;
      private IOBiConsumer<Long, Long> onMaxCount = IOBiConsumer.noop();
      private boolean propagateClose = true;

      long getCount() {
         return this.count;
      }

      long getMaxCount() {
         return this.maxCount;
      }

      IOBiConsumer<Long, Long> getOnMaxCount() {
         return this.onMaxCount;
      }

      boolean isPropagateClose() {
         return this.propagateClose;
      }

      public T setCount(long count) {
         this.count = Math.max(0L, count);
         return this.asThis();
      }

      public T setMaxCount(long maxCount) {
         this.maxCount = Math.max(-1L, maxCount);
         return this.asThis();
      }

      public T setOnMaxCount(IOBiConsumer<Long, Long> onMaxCount) {
         this.onMaxCount = onMaxCount != null ? onMaxCount : IOBiConsumer.noop();
         return this.asThis();
      }

      public T setPropagateClose(boolean propagateClose) {
         this.propagateClose = propagateClose;
         return this.asThis();
      }
   }

   public static class Builder extends BoundedInputStream.AbstractBuilder<BoundedInputStream.Builder> {
      public BoundedInputStream get() throws IOException {
         return new BoundedInputStream(this);
      }
   }
}
