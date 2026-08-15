package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

abstract class BaseMpscLinkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueuePad3<E> {
   protected @Nullable E[] producerBuffer;
   protected volatile long producerLimit;
   protected long producerMask = this.consumerMask;

   BaseMpscLinkedArrayQueueColdProducerFields(int initialCapacity) {
      super(initialCapacity);
      this.producerBuffer = this.consumerBuffer;
   }
}
