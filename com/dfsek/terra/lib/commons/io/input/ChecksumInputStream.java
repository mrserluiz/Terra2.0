package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.util.Objects;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

public final class ChecksumInputStream extends CountingInputStream {
   private final long expectedChecksumValue;
   private final long countThreshold;

   public static ChecksumInputStream.Builder builder() {
      return new ChecksumInputStream.Builder();
   }

   private ChecksumInputStream(ChecksumInputStream.Builder builder) throws IOException {
      super(new CheckedInputStream(builder.getInputStream(), Objects.requireNonNull(builder.checksum, "builder.checksum")), builder);
      this.countThreshold = builder.countThreshold;
      this.expectedChecksumValue = builder.expectedChecksumValue;
   }

   @Override
   protected synchronized void afterRead(int n) throws IOException {
      super.afterRead(n);
      if ((this.countThreshold > 0L && this.getByteCount() >= this.countThreshold || n == -1) && this.expectedChecksumValue != this.getChecksum().getValue()) {
         throw new IOException("Checksum verification failed.");
      }
   }

   private Checksum getChecksum() {
      return ((CheckedInputStream)this.in).getChecksum();
   }

   public long getRemaining() {
      return this.countThreshold - this.getByteCount();
   }

   public static class Builder extends ProxyInputStream.AbstractBuilder<ChecksumInputStream, ChecksumInputStream.Builder> {
      private Checksum checksum;
      private long countThreshold = -1L;
      private long expectedChecksumValue;

      public ChecksumInputStream get() throws IOException {
         return new ChecksumInputStream(this);
      }

      public ChecksumInputStream.Builder setChecksum(Checksum checksum) {
         this.checksum = checksum;
         return this;
      }

      public ChecksumInputStream.Builder setCountThreshold(long countThreshold) {
         this.countThreshold = countThreshold;
         return this;
      }

      public ChecksumInputStream.Builder setExpectedChecksumValue(long expectedChecksumValue) {
         this.expectedChecksumValue = expectedChecksumValue;
         return this;
      }
   }
}
