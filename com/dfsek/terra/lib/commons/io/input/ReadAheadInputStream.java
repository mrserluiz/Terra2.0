package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReadAheadInputStream extends FilterInputStream {
   private static final ThreadLocal<byte[]> BYTE_ARRAY_1 = ThreadLocal.withInitial(() -> new byte[1]);
   private final ReentrantLock stateChangeLock = new ReentrantLock();
   private ByteBuffer activeBuffer;
   private ByteBuffer readAheadBuffer;
   private boolean endOfStream;
   private boolean readInProgress;
   private boolean readAborted;
   private Throwable readException;
   private boolean isClosed;
   private boolean isUnderlyingInputStreamBeingClosed;
   private boolean isReading;
   private final AtomicBoolean isWaiting = new AtomicBoolean();
   private final ExecutorService executorService;
   private final boolean shutdownExecutorService;
   private final Condition asyncReadComplete = this.stateChangeLock.newCondition();

   public static ReadAheadInputStream.Builder builder() {
      return new ReadAheadInputStream.Builder();
   }

   private static Thread newDaemonThread(Runnable r) {
      Thread thread = new Thread(r, "commons-io-read-ahead");
      thread.setDaemon(true);
      return thread;
   }

   private static ExecutorService newExecutorService() {
      return Executors.newSingleThreadExecutor(ReadAheadInputStream::newDaemonThread);
   }

   @Deprecated
   public ReadAheadInputStream(InputStream inputStream, int bufferSizeInBytes) {
      this(inputStream, bufferSizeInBytes, newExecutorService(), true);
   }

   @Deprecated
   public ReadAheadInputStream(InputStream inputStream, int bufferSizeInBytes, ExecutorService executorService) {
      this(inputStream, bufferSizeInBytes, executorService, false);
   }

   private ReadAheadInputStream(InputStream inputStream, int bufferSizeInBytes, ExecutorService executorService, boolean shutdownExecutorService) {
      super(Objects.requireNonNull(inputStream, "inputStream"));
      if (bufferSizeInBytes <= 0) {
         throw new IllegalArgumentException("bufferSizeInBytes should be greater than 0, but the value is " + bufferSizeInBytes);
      }

      this.executorService = Objects.requireNonNull(executorService, "executorService");
      this.shutdownExecutorService = shutdownExecutorService;
      this.activeBuffer = ByteBuffer.allocate(bufferSizeInBytes);
      this.readAheadBuffer = ByteBuffer.allocate(bufferSizeInBytes);
      ((Buffer)this.activeBuffer).flip();
      ((Buffer)this.readAheadBuffer).flip();
   }

   @Override
   public int available() throws IOException {
      this.stateChangeLock.lock();

      try {
         return (int)Math.min(2147483647L, (long)this.activeBuffer.remaining() + this.readAheadBuffer.remaining());
      } finally {
         this.stateChangeLock.unlock();
      }
   }

   private void checkReadException() throws IOException {
      if (this.readAborted) {
         if (this.readException instanceof IOException) {
            throw (IOException)this.readException;
         } else {
            throw new IOException(this.readException);
         }
      }
   }

   @Override
   public void close() throws IOException {
      boolean isSafeToCloseUnderlyingInputStream = false;
      this.stateChangeLock.lock();

      try {
         if (this.isClosed) {
            return;
         }

         this.isClosed = true;
         if (!this.isReading) {
            isSafeToCloseUnderlyingInputStream = true;
            this.isUnderlyingInputStreamBeingClosed = true;
         }
      } finally {
         this.stateChangeLock.unlock();
      }

      if (this.shutdownExecutorService) {
         try {
            this.executorService.shutdownNow();
            this.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
            InterruptedIOException iio = new InterruptedIOException(e.getMessage());
            iio.initCause(e);
            throw iio;
         } finally {
            if (isSafeToCloseUnderlyingInputStream) {
               super.close();
            }
         }
      }
   }

   private void closeUnderlyingInputStreamIfNecessary() {
      boolean needToCloseUnderlyingInputStream = false;
      this.stateChangeLock.lock();

      try {
         this.isReading = false;
         if (this.isClosed && !this.isUnderlyingInputStreamBeingClosed) {
            needToCloseUnderlyingInputStream = true;
         }
      } finally {
         this.stateChangeLock.unlock();
      }

      if (needToCloseUnderlyingInputStream) {
         try {
            super.close();
         } catch (IOException var5) {
         }
      }
   }

   private boolean isEndOfStream() {
      return !this.activeBuffer.hasRemaining() && !this.readAheadBuffer.hasRemaining() && this.endOfStream;
   }

   @Override
   public int read() throws IOException {
      if (this.activeBuffer.hasRemaining()) {
         return this.activeBuffer.get() & 0xFF;
      }

      byte[] oneByteArray = BYTE_ARRAY_1.get();
      oneByteArray[0] = 0;
      return this.read(oneByteArray, 0, 1) == -1 ? -1 : oneByteArray[0] & 0xFF;
   }

   @Override
   public int read(byte[] b, int offset, int len) throws IOException {
      if (offset < 0 || len < 0 || len > b.length - offset) {
         throw new IndexOutOfBoundsException();
      }

      if (len == 0) {
         return 0;
      }

      if (!this.activeBuffer.hasRemaining()) {
         this.stateChangeLock.lock();

         try {
            this.waitForAsyncReadComplete();
            if (!this.readAheadBuffer.hasRemaining()) {
               this.readAsync();
               this.waitForAsyncReadComplete();
               if (this.isEndOfStream()) {
                  return -1;
               }
            }

            this.swapBuffers();
            this.readAsync();
         } finally {
            this.stateChangeLock.unlock();
         }
      }

      len = Math.min(len, this.activeBuffer.remaining());
      this.activeBuffer.get(b, offset, len);
      return len;
   }

   private void readAsync() throws IOException {
      this.stateChangeLock.lock();

      byte[] arr;
      label37: {
         try {
            arr = this.readAheadBuffer.array();
            if (!this.endOfStream && !this.readInProgress) {
               this.checkReadException();
               ((Buffer)this.readAheadBuffer).position(0);
               ((Buffer)this.readAheadBuffer).flip();
               this.readInProgress = true;
               break label37;
            }
         } finally {
            this.stateChangeLock.unlock();
         }

         return;
      }

      this.executorService.execute(() -> {
         this.stateChangeLock.lock();

         try {
            if (this.isClosed) {
               this.readInProgress = false;
               return;
            }

            this.isReading = true;
         } finally {
            this.stateChangeLock.unlock();
         }

         int read = 0;
         int off = 0;
         int len = arr.length;
         Throwable exception = null;

         try {
            do {
               read = this.in.read(arr, off, len);
               if (read <= 0) {
                  break;
               }

               off += read;
               len -= read;
            } while (len > 0 && !this.isWaiting.get());
         } catch (Throwable ex) {
            exception = ex;
            if (ex instanceof Error) {
               throw (Error)ex;
            }
         } finally {
            this.stateChangeLock.lock();

            try {
               ((Buffer)this.readAheadBuffer).limit(off);
               if (read < 0 || exception instanceof EOFException) {
                  this.endOfStream = true;
               } else if (exception != null) {
                  this.readAborted = true;
                  this.readException = exception;
               }

               this.readInProgress = false;
               this.signalAsyncReadComplete();
            } finally {
               this.stateChangeLock.unlock();
            }

            this.closeUnderlyingInputStreamIfNecessary();
         }
      });
   }

   private void signalAsyncReadComplete() {
      this.stateChangeLock.lock();

      try {
         this.asyncReadComplete.signalAll();
      } finally {
         this.stateChangeLock.unlock();
      }
   }

   @Override
   public long skip(long n) throws IOException {
      if (n <= 0L) {
         return 0L;
      }

      if (n <= this.activeBuffer.remaining()) {
         ((Buffer)this.activeBuffer).position((int)n + this.activeBuffer.position());
         return n;
      }

      this.stateChangeLock.lock();

      long skipped;
      try {
         skipped = this.skipInternal(n);
      } finally {
         this.stateChangeLock.unlock();
      }

      return skipped;
   }

   private long skipInternal(long n) throws IOException {
      if (!this.stateChangeLock.isLocked()) {
         throw new IllegalStateException("Expected stateChangeLock to be locked");
      }

      this.waitForAsyncReadComplete();
      if (this.isEndOfStream()) {
         return 0L;
      }

      if (this.available() >= n) {
         int toSkip = (int)n;
         toSkip -= this.activeBuffer.remaining();
         if (toSkip <= 0) {
            throw new IllegalStateException("Expected toSkip > 0, actual: " + toSkip);
         }

         ((Buffer)this.activeBuffer).position(0);
         ((Buffer)this.activeBuffer).flip();
         ((Buffer)this.readAheadBuffer).position(toSkip + this.readAheadBuffer.position());
         this.swapBuffers();
         this.readAsync();
         return n;
      } else {
         int skippedBytes = this.available();
         long toSkip = n - skippedBytes;
         ((Buffer)this.activeBuffer).position(0);
         ((Buffer)this.activeBuffer).flip();
         ((Buffer)this.readAheadBuffer).position(0);
         ((Buffer)this.readAheadBuffer).flip();
         long skippedFromInputStream = this.in.skip(toSkip);
         this.readAsync();
         return skippedBytes + skippedFromInputStream;
      }
   }

   private void swapBuffers() {
      ByteBuffer temp = this.activeBuffer;
      this.activeBuffer = this.readAheadBuffer;
      this.readAheadBuffer = temp;
   }

   private void waitForAsyncReadComplete() throws IOException {
      this.stateChangeLock.lock();

      try {
         this.isWaiting.set(true);

         while (this.readInProgress) {
            this.asyncReadComplete.await();
         }
      } catch (InterruptedException e) {
         InterruptedIOException iio = new InterruptedIOException(e.getMessage());
         iio.initCause(e);
         throw iio;
      } finally {
         try {
            this.isWaiting.set(false);
         } finally {
            this.stateChangeLock.unlock();
         }
      }

      this.checkReadException();
   }

   public static class Builder extends AbstractStreamBuilder<ReadAheadInputStream, ReadAheadInputStream.Builder> {
      private ExecutorService executorService;

      public ReadAheadInputStream get() throws IOException {
         return new ReadAheadInputStream(
            this.getInputStream(),
            this.getBufferSize(),
            this.executorService != null ? this.executorService : ReadAheadInputStream.newExecutorService(),
            this.executorService == null
         );
      }

      public ReadAheadInputStream.Builder setExecutorService(ExecutorService executorService) {
         this.executorService = executorService;
         return this;
      }
   }
}
