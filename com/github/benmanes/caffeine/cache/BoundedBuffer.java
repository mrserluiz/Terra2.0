package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

final class BoundedBuffer<E> extends StripedBuffer<E> {
   static final int BUFFER_SIZE = 16;
   static final int MASK = 15;

   @Override
   protected Buffer<E> create(E e) {
      return new BoundedBuffer.RingBuffer<>(e);
   }

   static final class RingBuffer<E> extends BBHeader.ReadAndWriteCounterRef implements Buffer<E> {
      static final VarHandle BUFFER = MethodHandles.arrayElementVarHandle(Object[].class);
      final Object[] buffer = new Object[16];

      public RingBuffer(E e) {
         BUFFER.set((Object[])this.buffer, (int)0, (Object)e);
         WRITE.set((BoundedBuffer.RingBuffer)this, (int)1);
      }

      @Override
      public int offer(E e) {
         long head = this.readCounter;
         long tail = this.writeCounterOpaque();
         long size = tail - head;
         if (size >= 16L) {
            return 1;
         } else if (this.casWriteCounter(tail, tail + 1L)) {
            int index = (int)(tail & 15L);
            BUFFER.setRelease((Object[])this.buffer, (int)index, (Object)e);
            return 0;
         } else {
            return -1;
         }
      }

      @Override
      public void drainTo(Consumer<E> consumer) {
         long head = this.readCounter;
         long tail = this.writeCounterOpaque();
         long size = tail - head;
         if (size != 0L) {
            do {
               int index = (int)(head & 15L);
               E e = (E)(Object)BUFFER.getAcquire((Object[])this.buffer, (int)index);
               if (e == null) {
                  break;
               }

               BUFFER.setRelease((Object[])this.buffer, (int)index, (Void)null);
               consumer.accept(e);
            } while (++head != tail);

            this.setReadCounterOpaque(head);
         }
      }

      @Override
      public long reads() {
         return this.readCounter;
      }

      @Override
      public long writes() {
         return this.writeCounter;
      }
   }
}
