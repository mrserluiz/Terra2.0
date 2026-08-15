package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.ListIterator;

@GwtCompatible
abstract class TransformedListIterator<F, T> extends TransformedIterator<F, T> implements ListIterator<T> {
   TransformedListIterator(ListIterator<? extends F> backingIterator) {
      super(backingIterator);
   }

   private ListIterator<? extends F> backingIterator() {
      return (ListIterator<? extends F>)this.backingIterator;
   }

   @Override
   public final boolean hasPrevious() {
      return this.backingIterator().hasPrevious();
   }

   @ParametricNullness
   @Override
   public final T previous() {
      return this.transform((F)this.backingIterator().previous());
   }

   @Override
   public final int nextIndex() {
      return this.backingIterator().nextIndex();
   }

   @Override
   public final int previousIndex() {
      return this.backingIterator().previousIndex();
   }

   @Override
   public void set(@ParametricNullness T element) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(@ParametricNullness T element) {
      throw new UnsupportedOperationException();
   }
}
