package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.input.QueueInputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueOutputStream extends OutputStream {
   private final BlockingQueue<Integer> blockingQueue;

   public QueueOutputStream() {
      this(new LinkedBlockingQueue<>());
   }

   public QueueOutputStream(BlockingQueue<Integer> blockingQueue) {
      this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
   }

   public QueueInputStream newQueueInputStream() {
      return QueueInputStream.builder().setBlockingQueue(this.blockingQueue).get();
   }

   @Override
   public void write(int b) throws InterruptedIOException {
      try {
         this.blockingQueue.put(0xFF & b);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         InterruptedIOException interruptedIoException = new InterruptedIOException();
         interruptedIoException.initCause(e);
         throw interruptedIoException;
      }
   }
}
