package com.dfsek.terra.lib.google.common.hash;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class AbstractStreamingHasher extends AbstractHasher {
   private final ByteBuffer buffer;
   private final int bufferSize;
   private final int chunkSize;

   protected AbstractStreamingHasher(int chunkSize) {
      this(chunkSize, chunkSize);
   }

   protected AbstractStreamingHasher(int chunkSize, int bufferSize) {
      Preconditions.checkArgument(bufferSize % chunkSize == 0);
      this.buffer = ByteBuffer.allocate(bufferSize + 7).order(ByteOrder.LITTLE_ENDIAN);
      this.bufferSize = bufferSize;
      this.chunkSize = chunkSize;
   }

   protected abstract void process(ByteBuffer bb);

   protected void processRemaining(ByteBuffer bb) {
      Java8Compatibility.position(bb, bb.limit());
      Java8Compatibility.limit(bb, this.chunkSize + 7);

      while (bb.position() < this.chunkSize) {
         bb.putLong(0L);
      }

      Java8Compatibility.limit(bb, this.chunkSize);
      Java8Compatibility.flip(bb);
      this.process(bb);
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putBytes(byte[] bytes, int off, int len) {
      return this.putBytesInternal(ByteBuffer.wrap(bytes, off, len).order(ByteOrder.LITTLE_ENDIAN));
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putBytes(ByteBuffer readBuffer) {
      ByteOrder order = readBuffer.order();

      try {
         readBuffer.order(ByteOrder.LITTLE_ENDIAN);
         return this.putBytesInternal(readBuffer);
      } finally {
         readBuffer.order(order);
      }
   }

   @CanIgnoreReturnValue
   private Hasher putBytesInternal(ByteBuffer readBuffer) {
      if (readBuffer.remaining() <= this.buffer.remaining()) {
         this.buffer.put(readBuffer);
         this.munchIfFull();
         return this;
      }

      int bytesToCopy = this.bufferSize - this.buffer.position();

      for (int i = 0; i < bytesToCopy; i++) {
         this.buffer.put(readBuffer.get());
      }

      this.munch();

      while (readBuffer.remaining() >= this.chunkSize) {
         this.process(readBuffer);
      }

      this.buffer.put(readBuffer);
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putByte(byte b) {
      this.buffer.put(b);
      this.munchIfFull();
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putShort(short s) {
      this.buffer.putShort(s);
      this.munchIfFull();
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putChar(char c) {
      this.buffer.putChar(c);
      this.munchIfFull();
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putInt(int i) {
      this.buffer.putInt(i);
      this.munchIfFull();
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putLong(long l) {
      this.buffer.putLong(l);
      this.munchIfFull();
      return this;
   }

   @Override
   public final HashCode hash() {
      this.munch();
      Java8Compatibility.flip(this.buffer);
      if (this.buffer.remaining() > 0) {
         this.processRemaining(this.buffer);
         Java8Compatibility.position(this.buffer, this.buffer.limit());
      }

      return this.makeHash();
   }

   protected abstract HashCode makeHash();

   private void munchIfFull() {
      if (this.buffer.remaining() < 8) {
         this.munch();
      }
   }

   private void munch() {
      Java8Compatibility.flip(this.buffer);

      while (this.buffer.remaining() >= this.chunkSize) {
         this.process(this.buffer);
      }

      this.buffer.compact();
   }
}
