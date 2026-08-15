package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

class MpscGrowableArrayQueue<E> extends MpscChunkedArrayQueue<E> {
   MpscGrowableArrayQueue(int initialCapacity, int maxCapacity) {
      super(initialCapacity, maxCapacity);
   }

   @Override
   protected int getNextBufferSize(@Nullable E[] buffer) {
      long maxSize = this.maxQueueCapacity / 2L;
      Caffeine.requireState(maxSize >= buffer.length);
      int newSize = 2 * (buffer.length - 1);
      return newSize + 1;
   }

   @Override
   protected long getCurrentBufferCapacity(long mask) {
      return mask + 2L == this.maxQueueCapacity ? this.maxQueueCapacity : mask;
   }
}
