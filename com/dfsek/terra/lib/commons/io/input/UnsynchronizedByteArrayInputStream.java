package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class UnsynchronizedByteArrayInputStream extends InputStream {
   public static final int END_OF_STREAM = -1;
   private final byte[] data;
   private final int eod;
   private int offset;
   private int markedOffset;

   public static UnsynchronizedByteArrayInputStream.Builder builder() {
      return new UnsynchronizedByteArrayInputStream.Builder();
   }

   private static int minPosLen(byte[] data, int defaultValue) {
      requireNonNegative(defaultValue, "defaultValue");
      return Math.min(defaultValue, data.length > 0 ? data.length : defaultValue);
   }

   private static int requireNonNegative(int value, String name) {
      if (value < 0) {
         throw new IllegalArgumentException(name + " cannot be negative");
      } else {
         return value;
      }
   }

   @Deprecated
   public UnsynchronizedByteArrayInputStream(byte[] data) {
      this(data, data.length, 0, 0);
   }

   @Deprecated
   public UnsynchronizedByteArrayInputStream(byte[] data, int offset) {
      this(data, data.length, Math.min(requireNonNegative(offset, "offset"), minPosLen(data, offset)), minPosLen(data, offset));
   }

   @Deprecated
   public UnsynchronizedByteArrayInputStream(byte[] data, int offset, int length) {
      requireNonNegative(offset, "offset");
      requireNonNegative(length, "length");
      this.data = Objects.requireNonNull(data, "data");
      this.eod = Math.min(minPosLen(data, offset) + length, data.length);
      this.offset = minPosLen(data, offset);
      this.markedOffset = minPosLen(data, offset);
   }

   private UnsynchronizedByteArrayInputStream(byte[] data, int eod, int offset, int markedOffset) {
      this.data = Objects.requireNonNull(data, "data");
      this.eod = eod;
      this.offset = offset;
      this.markedOffset = markedOffset;
   }

   @Override
   public int available() {
      return this.offset < this.eod ? this.eod - this.offset : 0;
   }

   @Override
   public void mark(int readLimit) {
      this.markedOffset = this.offset;
   }

   @Override
   public boolean markSupported() {
      return true;
   }

   @Override
   public int read() {
      return this.offset < this.eod ? this.data[this.offset++] & 0xFF : -1;
   }

   @Override
   public int read(byte[] dest) {
      Objects.requireNonNull(dest, "dest");
      return this.read(dest, 0, dest.length);
   }

   @Override
   public int read(byte[] dest, int off, int len) {
      Objects.requireNonNull(dest, "dest");
      if (off < 0 || len < 0 || off + len > dest.length) {
         throw new IndexOutOfBoundsException();
      }

      if (this.offset >= this.eod) {
         return -1;
      }

      int actualLen = this.eod - this.offset;
      if (len < actualLen) {
         actualLen = len;
      }

      if (actualLen <= 0) {
         return 0;
      }

      System.arraycopy(this.data, this.offset, dest, off, actualLen);
      this.offset += actualLen;
      return actualLen;
   }

   @Override
   public void reset() {
      this.offset = this.markedOffset;
   }

   @Override
   public long skip(long n) {
      if (n < 0L) {
         throw new IllegalArgumentException("Skipping backward is not supported");
      }

      long actualSkip = this.eod - this.offset;
      if (n < actualSkip) {
         actualSkip = n;
      }

      this.offset = Math.addExact(this.offset, Math.toIntExact(n));
      return actualSkip;
   }

   public static class Builder extends AbstractStreamBuilder<UnsynchronizedByteArrayInputStream, UnsynchronizedByteArrayInputStream.Builder> {
      private int offset;
      private int length;

      public UnsynchronizedByteArrayInputStream get() throws IOException {
         return new UnsynchronizedByteArrayInputStream(this.checkOrigin().getByteArray(), this.offset, this.length);
      }

      public UnsynchronizedByteArrayInputStream.Builder setByteArray(byte[] origin) {
         this.length = Objects.requireNonNull(origin, "origin").length;
         return (UnsynchronizedByteArrayInputStream.Builder)super.setByteArray(origin);
      }

      public UnsynchronizedByteArrayInputStream.Builder setLength(int length) {
         if (length < 0) {
            throw new IllegalArgumentException("length cannot be negative");
         }

         this.length = length;
         return this;
      }

      public UnsynchronizedByteArrayInputStream.Builder setOffset(int offset) {
         if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
         }

         this.offset = offset;
         return this;
      }
   }
}
