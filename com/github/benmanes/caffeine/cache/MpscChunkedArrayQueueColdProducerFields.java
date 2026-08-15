package com.github.benmanes.caffeine.cache;

abstract class MpscChunkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueue<E> {
   protected final long maxQueueCapacity;

   MpscChunkedArrayQueueColdProducerFields(int initialCapacity, int maxCapacity) {
      super(initialCapacity);
      Caffeine.requireArgument(maxCapacity >= 4, "Max capacity must be 4 or more");
      Caffeine.requireArgument(
         Caffeine.ceilingPowerOfTwo(maxCapacity) >= Caffeine.ceilingPowerOfTwo(initialCapacity),
         "Initial capacity cannot exceed maximum capacity(both rounded up to a power of 2)"
      );
      this.maxQueueCapacity = (long)Caffeine.ceilingPowerOfTwo(maxCapacity) << 1;
   }
}
