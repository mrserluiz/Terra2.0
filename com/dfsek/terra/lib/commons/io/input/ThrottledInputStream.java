package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class ThrottledInputStream extends CountingInputStream {
   private final double maxBytesPerSecond;
   private final long startTime = System.currentTimeMillis();
   private Duration totalSleepDuration = Duration.ZERO;

   public static ThrottledInputStream.Builder builder() {
      return new ThrottledInputStream.Builder();
   }

   static long toSleepMillis(long bytesRead, long elapsedMillis, double maxBytesPerSec) {
      if (bytesRead > 0L && !(maxBytesPerSec <= 0.0) && elapsedMillis != 0L) {
         long millis = (long)(bytesRead / maxBytesPerSec * 1000.0 - elapsedMillis);
         return millis <= 0L ? 0L : millis;
      } else {
         return 0L;
      }
   }

   private ThrottledInputStream(ThrottledInputStream.Builder builder) throws IOException {
      super(builder);
      if (builder.maxBytesPerSecond <= 0.0) {
         throw new IllegalArgumentException("Bandwidth " + builder.maxBytesPerSecond + " is invalid.");
      }

      this.maxBytesPerSecond = builder.maxBytesPerSecond;
   }

   @Override
   protected void beforeRead(int n) throws IOException {
      this.throttle();
   }

   private long getBytesPerSecond() {
      long elapsedSeconds = (System.currentTimeMillis() - this.startTime) / 1000L;
      return elapsedSeconds == 0L ? this.getByteCount() : this.getByteCount() / elapsedSeconds;
   }

   double getMaxBytesPerSecond() {
      return this.maxBytesPerSecond;
   }

   private long getSleepMillis() {
      return toSleepMillis(this.getByteCount(), System.currentTimeMillis() - this.startTime, this.maxBytesPerSecond);
   }

   Duration getTotalSleepDuration() {
      return this.totalSleepDuration;
   }

   private void throttle() throws InterruptedIOException {
      long sleepMillis = this.getSleepMillis();
      if (sleepMillis > 0L) {
         this.totalSleepDuration = this.totalSleepDuration.plus(sleepMillis, ChronoUnit.MILLIS);

         try {
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
         } catch (InterruptedException e) {
            throw new InterruptedIOException("Thread aborted");
         }
      }
   }

   @Override
   public String toString() {
      return "ThrottledInputStream[bytesRead="
         + this.getByteCount()
         + ", maxBytesPerSec="
         + this.maxBytesPerSecond
         + ", bytesPerSec="
         + this.getBytesPerSecond()
         + ", totalSleepDuration="
         + this.totalSleepDuration
         + ']';
   }

   public static class Builder extends ProxyInputStream.AbstractBuilder<ThrottledInputStream, ThrottledInputStream.Builder> {
      private double maxBytesPerSecond = Double.MAX_VALUE;

      public ThrottledInputStream get() throws IOException {
         return new ThrottledInputStream(this);
      }

      double getMaxBytesPerSecond() {
         return this.maxBytesPerSecond;
      }

      public ThrottledInputStream.Builder setMaxBytes(long value, ChronoUnit chronoUnit) {
         this.setMaxBytes(value, chronoUnit.getDuration());
         return this.asThis();
      }

      ThrottledInputStream.Builder setMaxBytes(long value, Duration duration) {
         this.setMaxBytesPerSecond(Objects.requireNonNull(duration, "duration").toMillis() / 1000.0 * value);
         return this.asThis();
      }

      private ThrottledInputStream.Builder setMaxBytesPerSecond(double maxBytesPerSecond) {
         if (maxBytesPerSecond <= 0.0) {
            throw new IllegalArgumentException("Bandwidth " + maxBytesPerSecond + " must be > 0.");
         }

         this.maxBytesPerSecond = maxBytesPerSecond;
         return this.asThis();
      }

      public void setMaxBytesPerSecond(long maxBytesPerSecond) {
         this.setMaxBytesPerSecond((double)maxBytesPerSecond);
      }
   }
}
