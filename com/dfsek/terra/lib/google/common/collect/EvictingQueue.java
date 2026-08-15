package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

@GwtCompatible
public final class EvictingQueue<E> extends ForwardingQueue<E> implements Serializable {
   private final Queue<E> delegate;
   @VisibleForTesting
   final int maxSize;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   private EvictingQueue(int maxSize) {
      Preconditions.checkArgument(maxSize >= 0, "maxSize (%s) must >= 0", maxSize);
      this.delegate = new ArrayDeque<>(maxSize);
      this.maxSize = maxSize;
   }

   public static <E> EvictingQueue<E> create(int maxSize) {
      return new EvictingQueue<>(maxSize);
   }

   public int remainingCapacity() {
      return this.maxSize - this.size();
   }

   @Override
   protected Queue<E> delegate() {
      return this.delegate;
   }

   @CanIgnoreReturnValue
   @Override
   public boolean offer(E e) {
      return this.add(e);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean add(E e) {
      Preconditions.checkNotNull(e);
      if (this.maxSize == 0) {
         return true;
      }

      if (this.size() == this.maxSize) {
         this.delegate.remove();
      }

      this.delegate.add(e);
      return true;
   }

   @CanIgnoreReturnValue
   @Override
   public boolean addAll(Collection<? extends E> collection) {
      int size = collection.size();
      if (size >= this.maxSize) {
         this.clear();
         return Iterables.addAll(this, Iterables.skip(collection, size - this.maxSize));
      } else {
         return this.standardAddAll(collection);
      }
   }

   @J2ktIncompatible
   @Override
   public Object[] toArray() {
      return super.toArray();
   }
}
