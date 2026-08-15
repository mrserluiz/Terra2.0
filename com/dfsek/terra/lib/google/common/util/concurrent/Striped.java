package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Supplier;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.Lists;
import com.dfsek.terra.lib.google.common.collect.MapMaker;
import com.dfsek.terra.lib.google.common.math.IntMath;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class Striped<L> {
   private static final int LARGE_LAZY_CUTOFF = 1024;
   private static final int ALL_SET = -1;

   private Striped() {
   }

   public abstract L get(Object key);

   public abstract L getAt(int index);

   abstract int indexFor(Object key);

   public abstract int size();

   public Iterable<L> bulkGet(Iterable<? extends Object> keys) {
      List<Object> result = Lists.newArrayList(keys);
      if (result.isEmpty()) {
         return ImmutableList.of();
      }

      int[] stripes = new int[result.size()];

      for (int i = 0; i < result.size(); i++) {
         stripes[i] = this.indexFor(result.get(i));
      }

      Arrays.sort(stripes);
      int previousStripe = stripes[0];
      result.set(0, this.getAt(previousStripe));

      for (int i = 1; i < result.size(); i++) {
         int currentStripe = stripes[i];
         if (currentStripe == previousStripe) {
            result.set(i, result.get(i - 1));
         } else {
            result.set(i, this.getAt(currentStripe));
            previousStripe = currentStripe;
         }
      }

      List<L> asStripes = (List<L>)result;
      return Collections.unmodifiableList(asStripes);
   }

   static <L> Striped<L> custom(int stripes, Supplier<L> supplier) {
      return new Striped.CompactStriped<>(stripes, supplier);
   }

   public static Striped<Lock> lock(int stripes) {
      return custom(stripes, Striped.PaddedLock::new);
   }

   public static Striped<Lock> lazyWeakLock(int stripes) {
      return lazyWeakCustom(stripes, () -> new ReentrantLock(false));
   }

   static <L> Striped<L> lazyWeakCustom(int stripes, Supplier<L> supplier) {
      return stripes < 1024 ? new Striped.SmallLazyStriped<>(stripes, supplier) : new Striped.LargeLazyStriped<>(stripes, supplier);
   }

   public static Striped<Semaphore> semaphore(int stripes, int permits) {
      return custom(stripes, () -> new Striped.PaddedSemaphore(permits));
   }

   public static Striped<Semaphore> lazyWeakSemaphore(int stripes, int permits) {
      return lazyWeakCustom(stripes, () -> new Semaphore(permits, false));
   }

   public static Striped<ReadWriteLock> readWriteLock(int stripes) {
      return custom(stripes, ReentrantReadWriteLock::new);
   }

   public static Striped<ReadWriteLock> lazyWeakReadWriteLock(int stripes) {
      return lazyWeakCustom(stripes, Striped.WeakSafeReadWriteLock::new);
   }

   private static int ceilToPowerOfTwo(int x) {
      return 1 << IntMath.log2(x, RoundingMode.CEILING);
   }

   private static int smear(int hashCode) {
      hashCode ^= hashCode >>> 20 ^ hashCode >>> 12;
      return hashCode ^ hashCode >>> 7 ^ hashCode >>> 4;
   }

   private static class CompactStriped<L> extends Striped.PowerOfTwoStriped<L> {
      private final Object[] array;

      private CompactStriped(int stripes, Supplier<L> supplier) {
         super(stripes);
         Preconditions.checkArgument(stripes <= 1073741824, "Stripes must be <= 2^30)");
         this.array = new Object[this.mask + 1];

         for (int i = 0; i < this.array.length; i++) {
            this.array[i] = supplier.get();
         }
      }

      @Override
      public L getAt(int index) {
         return (L)this.array[index];
      }

      @Override
      public int size() {
         return this.array.length;
      }
   }

   @VisibleForTesting
   static class LargeLazyStriped<L> extends Striped.PowerOfTwoStriped<L> {
      final ConcurrentMap<Integer, L> locks;
      final Supplier<L> supplier;
      final int size = this.mask == -1 ? Integer.MAX_VALUE : this.mask + 1;

      LargeLazyStriped(int stripes, Supplier<L> supplier) {
         super(stripes);
         this.supplier = supplier;
         this.locks = new MapMaker().weakValues().makeMap();
      }

      @Override
      public L getAt(int index) {
         if (this.size != Integer.MAX_VALUE) {
            Preconditions.checkElementIndex(index, this.size());
         }

         L existing = this.locks.get(index);
         if (existing != null) {
            return existing;
         }

         L created = this.supplier.get();
         existing = this.locks.putIfAbsent(index, created);
         return MoreObjects.firstNonNull(existing, created);
      }

      @Override
      public int size() {
         return this.size;
      }
   }

   private static class PaddedLock extends ReentrantLock {
      long unused1;
      long unused2;
      long unused3;

      PaddedLock() {
         super(false);
      }
   }

   private static class PaddedSemaphore extends Semaphore {
      long unused1;
      long unused2;
      long unused3;

      PaddedSemaphore(int permits) {
         super(permits, false);
      }
   }

   private abstract static class PowerOfTwoStriped<L> extends Striped<L> {
      final int mask;

      PowerOfTwoStriped(int stripes) {
         Preconditions.checkArgument(stripes > 0, "Stripes must be positive");
         this.mask = stripes > 1073741824 ? -1 : Striped.ceilToPowerOfTwo(stripes) - 1;
      }

      @Override
      final int indexFor(Object key) {
         int hash = Striped.smear(key.hashCode());
         return hash & this.mask;
      }

      @Override
      public final L get(Object key) {
         return this.getAt(this.indexFor(key));
      }
   }

   @VisibleForTesting
   static class SmallLazyStriped<L> extends Striped.PowerOfTwoStriped<L> {
      final AtomicReferenceArray<Striped.SmallLazyStriped.@Nullable ArrayReference<? extends L>> locks;
      final Supplier<L> supplier;
      final int size;
      final ReferenceQueue<L> queue = new ReferenceQueue<>();

      SmallLazyStriped(int stripes, Supplier<L> supplier) {
         super(stripes);
         this.size = this.mask == -1 ? Integer.MAX_VALUE : this.mask + 1;
         this.locks = new AtomicReferenceArray<>(this.size);
         this.supplier = supplier;
      }

      @Override
      public L getAt(int index) {
         if (this.size != Integer.MAX_VALUE) {
            Preconditions.checkElementIndex(index, this.size());
         }

         Striped.SmallLazyStriped.ArrayReference<? extends L> existingRef = this.locks.get(index);
         L existing = (L)(existingRef == null ? null : existingRef.get());
         if (existing != null) {
            return existing;
         }

         L created = this.supplier.get();
         Striped.SmallLazyStriped.ArrayReference<L> newRef = new Striped.SmallLazyStriped.ArrayReference<>(created, index, this.queue);

         while (!this.locks.compareAndSet(index, existingRef, newRef)) {
            existingRef = this.locks.get(index);
            existing = (L)(existingRef == null ? null : existingRef.get());
            if (existing != null) {
               return existing;
            }
         }

         this.drainQueue();
         return created;
      }

      private void drainQueue() {
         Reference<? extends L> ref;
         while ((ref = this.queue.poll()) != null) {
            Striped.SmallLazyStriped.ArrayReference<? extends L> arrayRef = (Striped.SmallLazyStriped.ArrayReference<? extends L>)ref;
            this.locks.compareAndSet(arrayRef.index, arrayRef, null);
         }
      }

      @Override
      public int size() {
         return this.size;
      }

      private static final class ArrayReference<L> extends WeakReference<L> {
         final int index;

         ArrayReference(L referent, int index, ReferenceQueue<L> queue) {
            super(referent, queue);
            this.index = index;
         }
      }
   }

   private static final class WeakSafeCondition extends ForwardingCondition {
      private final Condition delegate;
      private final Striped.WeakSafeReadWriteLock strongReference;

      WeakSafeCondition(Condition delegate, Striped.WeakSafeReadWriteLock strongReference) {
         this.delegate = delegate;
         this.strongReference = strongReference;
      }

      @Override
      Condition delegate() {
         return this.delegate;
      }
   }

   private static final class WeakSafeLock extends ForwardingLock {
      private final Lock delegate;
      private final Striped.WeakSafeReadWriteLock strongReference;

      WeakSafeLock(Lock delegate, Striped.WeakSafeReadWriteLock strongReference) {
         this.delegate = delegate;
         this.strongReference = strongReference;
      }

      @Override
      Lock delegate() {
         return this.delegate;
      }

      @Override
      public Condition newCondition() {
         return new Striped.WeakSafeCondition(this.delegate.newCondition(), this.strongReference);
      }
   }

   private static final class WeakSafeReadWriteLock implements ReadWriteLock {
      private final ReadWriteLock delegate = new ReentrantReadWriteLock();

      WeakSafeReadWriteLock() {
      }

      @Override
      public Lock readLock() {
         return new Striped.WeakSafeLock(this.delegate.readLock(), this);
      }

      @Override
      public Lock writeLock() {
         return new Striped.WeakSafeLock(this.delegate.writeLock(), this);
      }
   }
}
