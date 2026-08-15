package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ListIterator;

@GwtCompatible
public abstract class ForwardingListIterator<E> extends ForwardingIterator<E> implements ListIterator<E> {
   protected ForwardingListIterator() {
   }

   protected abstract ListIterator<E> delegate();

   @Override
   public void add(@ParametricNullness E element) {
      this.delegate().add(element);
   }

   @Override
   public boolean hasPrevious() {
      return this.delegate().hasPrevious();
   }

   @Override
   public int nextIndex() {
      return this.delegate().nextIndex();
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public E previous() {
      return this.delegate().previous();
   }

   @Override
   public int previousIndex() {
      return this.delegate().previousIndex();
   }

   @Override
   public void set(@ParametricNullness E element) {
      this.delegate().set(element);
   }
}
