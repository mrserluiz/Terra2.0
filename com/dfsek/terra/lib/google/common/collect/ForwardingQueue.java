package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingQueue<E> extends ForwardingCollection<E> implements Queue<E> {
   protected ForwardingQueue() {
   }

   protected abstract Queue<E> delegate();

   @CanIgnoreReturnValue
   @Override
   public boolean offer(@ParametricNullness E o) {
      return this.delegate().offer(o);
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable E poll() {
      return this.delegate().poll();
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public E remove() {
      return this.delegate().remove();
   }

   @Override
   public @Nullable E peek() {
      return this.delegate().peek();
   }

   @ParametricNullness
   @Override
   public E element() {
      return this.delegate().element();
   }

   protected boolean standardOffer(@ParametricNullness E e) {
      try {
         return this.add(e);
      } catch (IllegalStateException caught) {
         return false;
      }
   }

   protected @Nullable E standardPeek() {
      try {
         return this.element();
      } catch (NoSuchElementException caught) {
         return null;
      }
   }

   protected @Nullable E standardPoll() {
      try {
         return this.remove();
      } catch (NoSuchElementException caught) {
         return null;
      }
   }
}
