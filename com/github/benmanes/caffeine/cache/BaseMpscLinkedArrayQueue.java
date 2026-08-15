package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Iterator;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

abstract class BaseMpscLinkedArrayQueue<E> extends BaseMpscLinkedArrayQueueColdProducerFields<E> {
   static final VarHandle P_INDEX = findVarHandle(BaseMpscLinkedArrayQueueProducerFields.class, "producerIndex", long.class);
   static final VarHandle C_INDEX = findVarHandle(BaseMpscLinkedArrayQueueConsumerFields.class, "consumerIndex", long.class);
   static final VarHandle P_LIMIT = findVarHandle(BaseMpscLinkedArrayQueueColdProducerFields.class, "producerLimit", long.class);
   static final VarHandle REF_ARRAY = MethodHandles.arrayElementVarHandle(Object[].class);
   private static final Object JUMP = new Object();

   BaseMpscLinkedArrayQueue(int initialCapacity) {
      super(initialCapacity);
      soProducerLimit(this, this.producerMask);
   }

   @Override
   public final Iterator<E> iterator() {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString() {
      return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode());
   }

   @Override
   public boolean offer(E e) {
      Objects.requireNonNull(e);

      while (true) {
         long producerLimit = this.lvProducerLimit();
         long pIndex = lvProducerIndex(this);
         if ((pIndex & 1L) != 1L) {
            long mask = this.producerMask;
            E[] buffer = this.producerBuffer;
            if (producerLimit <= pIndex) {
               int result = this.offerSlowPath(mask, pIndex, producerLimit);
               switch (result) {
                  case 0:
                  default:
                     break;
                  case 1:
                     continue;
                  case 2:
                     return false;
                  case 3:
                     this.resize(mask, buffer, pIndex, e);
                     return true;
               }
            }

            if (casProducerIndex(this, pIndex, pIndex + 2L)) {
               producerLimit = modifiedCalcElementOffset(pIndex, mask);
               soElement(buffer, producerLimit, e);
               return true;
            }
            continue;
         }
      }
   }

   private int offerSlowPath(long mask, long pIndex, long producerLimit) {
      long cIndex = lvConsumerIndex(this);
      long bufferCapacity = this.getCurrentBufferCapacity(mask);
      int result = 0;
      if (cIndex + bufferCapacity > pIndex) {
         if (!casProducerLimit(this, producerLimit, cIndex + bufferCapacity)) {
            result = 1;
         }
      } else if (this.availableInQueue(pIndex, cIndex) <= 0L) {
         result = 2;
      } else if (casProducerIndex(this, pIndex, pIndex + 1L)) {
         result = 3;
      } else {
         result = 1;
      }

      return result;
   }

   protected abstract long availableInQueue(long pIndex, long cIndex);

   @Override
   public E poll() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = lvElement(buffer, offset);
      if (e == null) {
         if (index == lvProducerIndex(this)) {
            return null;
         }

         do {
            e = lvElement(buffer, offset);
         } while (e == null);
      }

      if (e == JUMP) {
         E[] nextBuffer = this.getNextBuffer(buffer, mask);
         return this.newBufferPoll(nextBuffer, index);
      } else {
         soElement(buffer, offset, null);
         soConsumerIndex(this, index + 2L);
         return (E)e;
      }
   }

   @Override
   public E peek() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = lvElement(buffer, offset);
      if (e == null && index != lvProducerIndex(this)) {
         while ((e = lvElement(buffer, offset)) == null) {
         }
      }

      return (E)(e == JUMP ? this.newBufferPeek(this.getNextBuffer(buffer, mask), index) : e);
   }

   private E[] getNextBuffer(E[] buffer, long mask) {
      long nextArrayOffset = nextArrayOffset(mask);
      E[] nextBuffer = (E[])((Object[])lvElement(buffer, nextArrayOffset));
      soElement(buffer, nextArrayOffset, null);
      return (E[])((Object[])Objects.requireNonNull(nextBuffer));
   }

   private static long nextArrayOffset(long mask) {
      return modifiedCalcElementOffset(mask + 2L, Long.MAX_VALUE);
   }

   private E newBufferPoll(E[] nextBuffer, long index) {
      long offsetInNew = this.newBufferAndOffset(nextBuffer, index);
      E n = lvElement(nextBuffer, offsetInNew);
      Objects.requireNonNull(n, "new buffer must have at least one element");
      soElement(nextBuffer, offsetInNew, null);
      soConsumerIndex(this, index + 2L);
      return n;
   }

   private E newBufferPeek(E[] nextBuffer, long index) {
      long offsetInNew = this.newBufferAndOffset(nextBuffer, index);
      E n = lvElement(nextBuffer, offsetInNew);
      Objects.requireNonNull(n, "new buffer must have at least one element");
      return n;
   }

   private long newBufferAndOffset(@Nullable E[] nextBuffer, long index) {
      this.consumerBuffer = nextBuffer;
      this.consumerMask = nextBuffer.length - 2L << 1;
      return modifiedCalcElementOffset(index, this.consumerMask);
   }

   @Override
   public final int size() {
      long after = lvConsumerIndex(this);

      long before;
      long currentProducerIndex;
      do {
         before = after;
         currentProducerIndex = lvProducerIndex(this);
         after = lvConsumerIndex(this);
      } while (before != after);

      long size = currentProducerIndex - after >> 1;
      return (int)Math.min(size, 2147483647L);
   }

   @Override
   public final boolean isEmpty() {
      return lvConsumerIndex(this) == lvProducerIndex(this);
   }

   private long lvProducerLimit() {
      return this.producerLimit;
   }

   public long currentProducerIndex() {
      return lvProducerIndex(this) / 2L;
   }

   public long currentConsumerIndex() {
      return lvConsumerIndex(this) / 2L;
   }

   public abstract int capacity();

   public boolean relaxedOffer(E e) {
      return this.offer(e);
   }

   public E relaxedPoll() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = lvElement(buffer, offset);
      if (e == null) {
         return null;
      } else if (e == JUMP) {
         E[] nextBuffer = this.getNextBuffer(buffer, mask);
         return this.newBufferPoll(nextBuffer, index);
      } else {
         soElement(buffer, offset, null);
         soConsumerIndex(this, index + 2L);
         return (E)e;
      }
   }

   public E relaxedPeek() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = lvElement(buffer, offset);
      return (E)(e == JUMP ? this.newBufferPeek(this.getNextBuffer(buffer, mask), index) : e);
   }

   private void resize(long oldMask, E[] oldBuffer, long pIndex, E e) {
      int newBufferLength = this.getNextBufferSize(oldBuffer);
      E[] newBuffer = (E[])allocate(newBufferLength);
      this.producerBuffer = newBuffer;
      int newMask = newBufferLength - 2 << 1;
      this.producerMask = newMask;
      long offsetInOld = modifiedCalcElementOffset(pIndex, oldMask);
      long offsetInNew = modifiedCalcElementOffset(pIndex, newMask);
      soElement(newBuffer, offsetInNew, e);
      soElement(oldBuffer, nextArrayOffset(oldMask), (E)newBuffer);
      long cIndex = lvConsumerIndex(this);
      long availableInQueue = this.availableInQueue(pIndex, cIndex);
      Caffeine.requireState(availableInQueue > 0L);
      soProducerLimit(this, pIndex + Math.min(newMask, availableInQueue));
      soProducerIndex(this, pIndex + 2L);
      soElement(oldBuffer, offsetInOld, JUMP);
   }

   protected abstract int getNextBufferSize(@Nullable E[] buffer);

   protected abstract long getCurrentBufferCapacity(long mask);

   static long lvProducerIndex(BaseMpscLinkedArrayQueue<?> self) {
      return (long)P_INDEX.getVolatile((BaseMpscLinkedArrayQueue)self);
   }

   static long lvConsumerIndex(BaseMpscLinkedArrayQueue<?> self) {
      return (long)C_INDEX.getVolatile((BaseMpscLinkedArrayQueue)self);
   }

   static void soProducerIndex(BaseMpscLinkedArrayQueue<?> self, long v) {
      P_INDEX.setRelease((BaseMpscLinkedArrayQueue)self, (long)v);
   }

   static boolean casProducerIndex(BaseMpscLinkedArrayQueue<?> self, long expect, long newValue) {
      return P_INDEX.compareAndSet((BaseMpscLinkedArrayQueue)self, (long)expect, (long)newValue);
   }

   static void soConsumerIndex(BaseMpscLinkedArrayQueue<?> self, long v) {
      C_INDEX.setRelease((BaseMpscLinkedArrayQueue)self, (long)v);
   }

   static boolean casProducerLimit(BaseMpscLinkedArrayQueue<?> self, long expect, long newValue) {
      return P_LIMIT.compareAndSet((BaseMpscLinkedArrayQueue)self, (long)expect, (long)newValue);
   }

   static void soProducerLimit(BaseMpscLinkedArrayQueue<?> self, long v) {
      P_LIMIT.setRelease((BaseMpscLinkedArrayQueue)self, (long)v);
   }

   static <E> void soElement(@Nullable E[] buffer, long offset, @Nullable E e) {
      REF_ARRAY.setRelease((Object[])buffer, (int)((int)offset), (Object)e);
   }

   static <E> @Nullable E lvElement(@Nullable E @Nullable [] buffer, long offset) {
      return (E)(Object)REF_ARRAY.getVolatile((Object[])buffer, (int)((int)offset));
   }

   static long modifiedCalcElementOffset(long index, long mask) {
      return (index & mask) >> 1;
   }

   static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
      try {
         Lookup lookup = MethodHandles.privateLookupIn(recv, MethodHandles.lookup());
         return lookup.findVarHandle(recv, name, type);
      } catch (ReflectiveOperationException e) {
         throw new ExceptionInInitializerError(e);
      }
   }
}
