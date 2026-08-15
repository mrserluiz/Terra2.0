package com.github.benmanes.caffeine.cache;

import com.google.errorprone.annotations.Var;
import java.lang.ref.ReferenceQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

final class TimerWheel<K, V> implements Iterable<Node<K, V>> {
   static final int[] BUCKETS = new int[]{64, 64, 32, 4, 1};
   static final long[] SPANS = new long[]{
      Caffeine.ceilingPowerOfTwo(TimeUnit.SECONDS.toNanos(1L)),
      Caffeine.ceilingPowerOfTwo(TimeUnit.MINUTES.toNanos(1L)),
      Caffeine.ceilingPowerOfTwo(TimeUnit.HOURS.toNanos(1L)),
      Caffeine.ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1L)),
      BUCKETS[3] * Caffeine.ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1L)),
      BUCKETS[3] * Caffeine.ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1L))
   };
   static final long[] SHIFT = new long[]{
      Long.numberOfTrailingZeros(SPANS[0]),
      Long.numberOfTrailingZeros(SPANS[1]),
      Long.numberOfTrailingZeros(SPANS[2]),
      Long.numberOfTrailingZeros(SPANS[3]),
      Long.numberOfTrailingZeros(SPANS[4])
   };
   final Node<K, V>[][] wheel = new Node[BUCKETS.length][];
   long nanos;

   TimerWheel() {
      for (int i = 0; i < this.wheel.length; i++) {
         this.wheel[i] = new Node[BUCKETS[i]];

         for (int j = 0; j < this.wheel[i].length; j++) {
            this.wheel[i][j] = new TimerWheel.Sentinel<>();
         }
      }
   }

   public void advance(BoundedLocalCache<K, V> cache, @Var long currentTimeNanos) {
      long previousTimeNanos = this.nanos;
      this.nanos = currentTimeNanos;
      if (previousTimeNanos < 0L && currentTimeNanos > 0L) {
         previousTimeNanos += Long.MAX_VALUE;
         currentTimeNanos += Long.MAX_VALUE;
      }

      try {
         for (int i = 0; i < SHIFT.length; i++) {
            long previousTicks = previousTimeNanos >>> (int)SHIFT[i];
            long currentTicks = currentTimeNanos >>> (int)SHIFT[i];
            long delta = currentTicks - previousTicks;
            if (delta <= 0L) {
               break;
            }

            this.expire(cache, i, previousTicks, delta);
         }
      } catch (Throwable t) {
         this.nanos = previousTimeNanos;
         throw t;
      }
   }

   void expire(BoundedLocalCache<K, V> cache, int index, long previousTicks, long delta) {
      Node<K, V>[] timerWheel = this.wheel[index];
      int mask = timerWheel.length - 1;
      int steps = Math.min(1 + (int)delta, timerWheel.length);
      int start = (int)(previousTicks & mask);
      int end = start + steps;

      for (int i = start; i < end; i++) {
         Node<K, V> sentinel = timerWheel[i & mask];
         Node<K, V> prev = sentinel.getPreviousInVariableOrder();
         Node<K, V> node = sentinel.getNextInVariableOrder();
         sentinel.setPreviousInVariableOrder(sentinel);
         sentinel.setNextInVariableOrder(sentinel);

         while (node != sentinel) {
            Node<K, V> next = node.getNextInVariableOrder();
            node.setPreviousInVariableOrder(null);
            node.setNextInVariableOrder(null);

            try {
               if (node.getVariableTime() - this.nanos > 0L || !cache.evictEntry(node, RemovalCause.EXPIRED, this.nanos)) {
                  this.schedule(node);
               }

               node = next;
            } catch (Throwable t) {
               node.setPreviousInVariableOrder(sentinel.getPreviousInVariableOrder());
               node.setNextInVariableOrder(next);
               sentinel.getPreviousInVariableOrder().setNextInVariableOrder(node);
               sentinel.setPreviousInVariableOrder(prev);
               throw t;
            }
         }
      }
   }

   public void schedule(Node<K, V> node) {
      Node<K, V> sentinel = this.findBucket(node.getVariableTime());
      this.link(sentinel, node);
   }

   public void reschedule(Node<K, V> node) {
      if (node.getNextInVariableOrder() != null) {
         this.unlink(node);
         this.schedule(node);
      }
   }

   public void deschedule(Node<K, V> node) {
      this.unlink(node);
      node.setNextInVariableOrder(null);
      node.setPreviousInVariableOrder(null);
   }

   Node<K, V> findBucket(@Var long time) {
      long duration = Math.max(0L, time - this.nanos);
      if (duration == 0L) {
         time = this.nanos;
      }

      int length = this.wheel.length - 1;

      for (int i = 0; i < length; i++) {
         if (duration < SPANS[i + 1]) {
            long ticks = time >>> (int)SHIFT[i];
            int index = (int)(ticks & this.wheel[i].length - 1);
            return this.wheel[i][index];
         }
      }

      return this.wheel[length][0];
   }

   void link(Node<K, V> sentinel, Node<K, V> node) {
      node.setPreviousInVariableOrder(sentinel.getPreviousInVariableOrder());
      node.setNextInVariableOrder(sentinel);
      sentinel.getPreviousInVariableOrder().setNextInVariableOrder(node);
      sentinel.setPreviousInVariableOrder(node);
   }

   void unlink(Node<K, V> node) {
      Node<K, V> next = node.getNextInVariableOrder();
      if (next != null) {
         Node<K, V> prev = node.getPreviousInVariableOrder();
         next.setPreviousInVariableOrder(prev);
         prev.setNextInVariableOrder(next);
      }
   }

   public long getExpirationDelay() {
      for (int i = 0; i < SHIFT.length; i++) {
         Node<K, V>[] timerWheel = this.wheel[i];
         long ticks = this.nanos >>> (int)SHIFT[i];
         long spanMask = SPANS[i] - 1L;
         int start = (int)(ticks & spanMask);
         int end = start + timerWheel.length;
         int mask = timerWheel.length - 1;

         for (int j = start; j < end; j++) {
            Node<K, V> sentinel = timerWheel[j & mask];
            Node<K, V> next = sentinel.getNextInVariableOrder();
            if (next != sentinel) {
               long buckets = j - start;
               long delay = (buckets << (int)SHIFT[i]) - (this.nanos & spanMask);
               delay = delay > 0L ? delay : SPANS[i];

               for (int k = i + 1; k < SHIFT.length; k++) {
                  long nextDelay = this.peekAhead(k);
                  delay = Math.min(delay, nextDelay);
               }

               return delay;
            }
         }
      }

      return Long.MAX_VALUE;
   }

   long peekAhead(int index) {
      long ticks = this.nanos >>> (int)SHIFT[index];
      Node<K, V>[] timerWheel = this.wheel[index];
      long spanMask = SPANS[index] - 1L;
      int mask = timerWheel.length - 1;
      int probe = (int)(ticks + 1L & mask);
      Node<K, V> sentinel = timerWheel[probe];
      Node<K, V> next = sentinel.getNextInVariableOrder();
      return next == sentinel ? Long.MAX_VALUE : SPANS[index] - (this.nanos & spanMask);
   }

   @Override
   public Iterator<Node<K, V>> iterator() {
      return new TimerWheel.AscendingIterator();
   }

   public Iterator<Node<K, V>> descendingIterator() {
      return new TimerWheel.DescendingIterator();
   }

   final class AscendingIterator extends TimerWheel<K, V>.Traverser {
      int wheelIndex;
      int steps;

      @Override
      boolean isDone() {
         return this.wheelIndex == TimerWheel.this.wheel.length;
      }

      @Override
      Node<K, V> sentinel() {
         return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
      }

      @Override
      Node<K, V> traverse(Node<K, V> node) {
         return node.getNextInVariableOrder();
      }

      @Override
      @Nullable Node<K, V> goToNextBucket() {
         return ++this.steps < TimerWheel.this.wheel[this.wheelIndex].length ? TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()] : null;
      }

      @Override
      @Nullable Node<K, V> goToNextWheel() {
         if (++this.wheelIndex == TimerWheel.this.wheel.length) {
            return null;
         }

         this.steps = 0;
         return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
      }

      int bucketIndex() {
         int ticks = (int)(TimerWheel.this.nanos >>> (int)TimerWheel.SHIFT[this.wheelIndex]);
         int bucketMask = TimerWheel.this.wheel[this.wheelIndex].length - 1;
         int bucketOffset = (ticks & bucketMask) + 1;
         return bucketOffset + this.steps & bucketMask;
      }
   }

   final class DescendingIterator extends TimerWheel<K, V>.Traverser {
      int wheelIndex = TimerWheel.this.wheel.length - 1;
      int steps;

      @Override
      boolean isDone() {
         return this.wheelIndex == -1;
      }

      @Override
      Node<K, V> sentinel() {
         return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
      }

      @Override
      @Nullable Node<K, V> goToNextBucket() {
         return ++this.steps < TimerWheel.this.wheel[this.wheelIndex].length ? TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()] : null;
      }

      @Override
      @Nullable Node<K, V> goToNextWheel() {
         if (--this.wheelIndex < 0) {
            return null;
         }

         this.steps = 0;
         return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
      }

      @Override
      Node<K, V> traverse(Node<K, V> node) {
         return node.getPreviousInVariableOrder();
      }

      int bucketIndex() {
         int ticks = (int)(TimerWheel.this.nanos >>> (int)TimerWheel.SHIFT[this.wheelIndex]);
         int bucketMask = TimerWheel.this.wheel[this.wheelIndex].length - 1;
         int bucketOffset = ticks & bucketMask;
         return bucketOffset - this.steps & bucketMask;
      }
   }

   static final class Sentinel<K, V> extends Node<K, V> {
      Node<K, V> prev;
      Node<K, V> next;

      Sentinel() {
         this.prev = this.next = this;
      }

      @Override
      public Node<K, V> getPreviousInVariableOrder() {
         return this.prev;
      }

      @Override
      public void setPreviousInVariableOrder(@Nullable Node<K, V> prev) {
         this.prev = prev;
      }

      @Override
      public Node<K, V> getNextInVariableOrder() {
         return this.next;
      }

      @Override
      public void setNextInVariableOrder(@Nullable Node<K, V> next) {
         this.next = next;
      }

      @Override
      public @Nullable K getKey() {
         return null;
      }

      @Override
      public Object getKeyReference() {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V getValue() {
         return null;
      }

      @Override
      public Object getValueReference() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setValue(V value, @Nullable ReferenceQueue<V> referenceQueue) {
      }

      @Override
      public boolean containsValue(Object value) {
         return false;
      }

      @Override
      public boolean isAlive() {
         return false;
      }

      @Override
      public boolean isRetired() {
         return false;
      }

      @Override
      public boolean isDead() {
         return false;
      }

      @Override
      public void retire() {
      }

      @Override
      public void die() {
      }
   }

   abstract class Traverser implements Iterator<Node<K, V>> {
      final long expectedNanos = TimerWheel.this.nanos;
      @Nullable Node<K, V> current;
      @Nullable Node<K, V> next;

      @Override
      public boolean hasNext() {
         if (TimerWheel.this.nanos != this.expectedNanos) {
            throw new ConcurrentModificationException();
         }

         if (this.next != null) {
            return true;
         }

         if (this.isDone()) {
            return false;
         }

         this.next = this.computeNext();
         return this.next != null;
      }

      public Node<K, V> next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         this.current = this.next;
         this.next = null;
         return Objects.requireNonNull(this.current);
      }

      @Nullable Node<K, V> computeNext() {
         Node<K, V> node = this.current == null ? this.sentinel() : this.current;

         do {
            node = this.traverse(node);
            if (node != this.sentinel()) {
               return node;
            }
         } while ((node = this.goToNextBucket()) != null || (node = this.goToNextWheel()) != null);

         return null;
      }

      abstract boolean isDone();

      abstract Node<K, V> sentinel();

      abstract Node<K, V> traverse(Node<K, V> node);

      abstract @Nullable Node<K, V> goToNextBucket();

      abstract @Nullable Node<K, V> goToNextWheel();
   }
}
