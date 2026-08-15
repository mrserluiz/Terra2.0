package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

abstract class BaseMpscLinkedArrayQueueConsumerFields<E> extends BaseMpscLinkedArrayQueuePad2<E> {
   protected @Nullable E[] consumerBuffer;
   protected long consumerIndex;
   protected long consumerMask;

   BaseMpscLinkedArrayQueueConsumerFields(int initialCapacity) {
      Caffeine.requireArgument(initialCapacity >= 2, "Initial capacity must be 2 or more");
      int p2capacity = Caffeine.ceilingPowerOfTwo(initialCapacity);
      long mask = p2capacity - 1L << 1;
      E[] buffer = (E[])allocate(p2capacity + 1);
      this.consumerBuffer = buffer;
      this.consumerMask = mask;
   }

   public static <E> @Nullable E[] allocate(int capacity) {
      return (E[])(new Object[capacity]);
   }
}
