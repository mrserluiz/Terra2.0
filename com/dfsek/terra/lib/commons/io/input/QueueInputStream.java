package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.output.QueueOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueInputStream extends InputStream {
   private final BlockingQueue<Integer> blockingQueue;
   private final long timeoutNanos;

   public static QueueInputStream.Builder builder() {
      return new QueueInputStream.Builder();
   }

   public QueueInputStream() {
      this(new LinkedBlockingQueue<>());
   }

   @Deprecated
   public QueueInputStream(BlockingQueue<Integer> blockingQueue) {
      this(builder().setBlockingQueue(blockingQueue));
   }

   private QueueInputStream(QueueInputStream.Builder builder) {
      this.blockingQueue = Objects.requireNonNull(builder.blockingQueue, "blockingQueue");
      this.timeoutNanos = Objects.requireNonNull(builder.timeout, "timeout").toNanos();
   }

   BlockingQueue<Integer> getBlockingQueue() {
      return this.blockingQueue;
   }

   Duration getTimeout() {
      return Duration.ofNanos(this.timeoutNanos);
   }

   public QueueOutputStream newQueueOutputStream() {
      return new QueueOutputStream(this.blockingQueue);
   }

   @Override
   public int read() {
      try {
         Integer value = this.blockingQueue.poll(this.timeoutNanos, TimeUnit.NANOSECONDS);
         return value == null ? -1 : 0xFF & value;
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException(e);
      }
   }

   public static class Builder extends AbstractStreamBuilder<QueueInputStream, QueueInputStream.Builder> {
      private BlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<>();
      private Duration timeout = Duration.ZERO;

      public QueueInputStream get() {
         return new QueueInputStream(this);
      }

      public QueueInputStream.Builder setBlockingQueue(BlockingQueue<Integer> blockingQueue) {
         this.blockingQueue = blockingQueue != null ? blockingQueue : new LinkedBlockingQueue<>();
         return this;
      }

      public QueueInputStream.Builder setTimeout(Duration timeout) {
         if (timeout != null && timeout.toNanos() < 0L) {
            throw new IllegalArgumentException("timeout must not be negative");
         }

         this.timeout = timeout != null ? timeout : Duration.ZERO;
         return this;
      }
   }
}
