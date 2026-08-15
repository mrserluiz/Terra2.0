package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.collect.ForwardingQueue;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class ForwardingBlockingQueue<E> extends ForwardingQueue<E> implements BlockingQueue<E> {
   protected ForwardingBlockingQueue() {
   }

   protected abstract BlockingQueue<E> delegate();

   @CanIgnoreReturnValue
   @Override
   public int drainTo(Collection<? super E> c, int maxElements) {
      return this.delegate().drainTo(c, maxElements);
   }

   @CanIgnoreReturnValue
   @Override
   public int drainTo(Collection<? super E> c) {
      return this.delegate().drainTo(c);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().offer(e, timeout, unit);
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable E poll(long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().poll(timeout, unit);
   }

   @Override
   public void put(E e) throws InterruptedException {
      this.delegate().put(e);
   }

   @Override
   public int remainingCapacity() {
      return this.delegate().remainingCapacity();
   }

   @CanIgnoreReturnValue
   @Override
   public E take() throws InterruptedException {
      return this.delegate().take();
   }
}
