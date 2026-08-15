package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

@Deprecated
@J2ktIncompatible
@GwtIncompatible
public abstract class ForwardingBlockingDeque<E> extends ForwardingDeque<E> implements BlockingDeque<E> {
   protected ForwardingBlockingDeque() {
   }

   protected abstract BlockingDeque<E> delegate();

   @Override
   public int remainingCapacity() {
      return this.delegate().remainingCapacity();
   }

   @Override
   public void putFirst(E e) throws InterruptedException {
      this.delegate().putFirst(e);
   }

   @Override
   public void putLast(E e) throws InterruptedException {
      this.delegate().putLast(e);
   }

   @Override
   public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().offerFirst(e, timeout, unit);
   }

   @Override
   public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().offerLast(e, timeout, unit);
   }

   @Override
   public E takeFirst() throws InterruptedException {
      return this.delegate().takeFirst();
   }

   @Override
   public E takeLast() throws InterruptedException {
      return this.delegate().takeLast();
   }

   @Override
   public @Nullable E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().pollFirst(timeout, unit);
   }

   @Override
   public @Nullable E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().pollLast(timeout, unit);
   }

   @Override
   public void put(E e) throws InterruptedException {
      this.delegate().put(e);
   }

   @Override
   public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().offer(e, timeout, unit);
   }

   @Override
   public E take() throws InterruptedException {
      return this.delegate().take();
   }

   @Override
   public @Nullable E poll(long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate().poll(timeout, unit);
   }

   @Override
   public int drainTo(Collection<? super E> c) {
      return this.delegate().drainTo(c);
   }

   @Override
   public int drainTo(Collection<? super E> c, int maxElements) {
      return this.delegate().drainTo(c, maxElements);
   }
}
