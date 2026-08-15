package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.function.IOConsumer;
import com.dfsek.terra.lib.commons.io.function.IOFunction;
import java.io.IOException;
import java.io.OutputStream;

public class ThresholdingOutputStream extends OutputStream {
   private static final IOFunction<ThresholdingOutputStream, OutputStream> NOOP_OS_GETTER = os -> NullOutputStream.INSTANCE;
   private final int threshold;
   private final IOConsumer<ThresholdingOutputStream> thresholdConsumer;
   private final IOFunction<ThresholdingOutputStream, OutputStream> outputStreamGetter;
   private long written;
   private boolean thresholdExceeded;

   public ThresholdingOutputStream(int threshold) {
      this(threshold, IOConsumer.noop(), NOOP_OS_GETTER);
   }

   public ThresholdingOutputStream(
      int threshold, IOConsumer<ThresholdingOutputStream> thresholdConsumer, IOFunction<ThresholdingOutputStream, OutputStream> outputStreamGetter
   ) {
      this.threshold = threshold < 0 ? 0 : threshold;
      this.thresholdConsumer = thresholdConsumer == null ? IOConsumer.noop() : thresholdConsumer;
      this.outputStreamGetter = outputStreamGetter == null ? NOOP_OS_GETTER : outputStreamGetter;
   }

   protected void checkThreshold(int count) throws IOException {
      if (!this.thresholdExceeded && this.written + count > this.threshold) {
         this.thresholdExceeded = true;
         this.thresholdReached();
      }
   }

   @Override
   public void close() throws IOException {
      try {
         this.flush();
      } catch (IOException var2) {
      }

      this.getStream().close();
   }

   @Override
   public void flush() throws IOException {
      this.getStream().flush();
   }

   public long getByteCount() {
      return this.written;
   }

   protected OutputStream getOutputStream() throws IOException {
      return this.outputStreamGetter.apply(this);
   }

   @Deprecated
   protected OutputStream getStream() throws IOException {
      return this.getOutputStream();
   }

   public int getThreshold() {
      return this.threshold;
   }

   public boolean isThresholdExceeded() {
      return this.written > this.threshold;
   }

   protected void resetByteCount() {
      this.thresholdExceeded = false;
      this.written = 0L;
   }

   protected void setByteCount(long count) {
      this.written = count;
   }

   protected void thresholdReached() throws IOException {
      this.thresholdConsumer.accept(this);
   }

   @Override
   public void write(byte[] b) throws IOException {
      this.checkThreshold(b.length);
      this.getStream().write(b);
      this.written += b.length;
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      this.checkThreshold(len);
      this.getStream().write(b, off, len);
      this.written += len;
   }

   @Override
   public void write(int b) throws IOException {
      this.checkThreshold(1);
      this.getStream().write(b);
      this.written++;
   }
}
