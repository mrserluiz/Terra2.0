package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractIterator<T> implements Iterator<T> {
   private AbstractIterator.State state = AbstractIterator.State.NOT_READY;
   private @Nullable T next;

   protected AbstractIterator() {
   }

   protected abstract @Nullable T computeNext();

   @CanIgnoreReturnValue
   protected final @Nullable T endOfData() {
      this.state = AbstractIterator.State.DONE;
      return null;
   }

   @Override
   public final boolean hasNext() {
      Preconditions.checkState(this.state != AbstractIterator.State.FAILED);
      switch (this.state) {
         case READY:
            return true;
         case DONE:
            return false;
         default:
            return this.tryToComputeNext();
      }
   }

   private boolean tryToComputeNext() {
      this.state = AbstractIterator.State.FAILED;
      this.next = this.computeNext();
      if (this.state != AbstractIterator.State.DONE) {
         this.state = AbstractIterator.State.READY;
         return true;
      } else {
         return false;
      }
   }

   @ParametricNullness
   @Override
   public final T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      }

      this.state = AbstractIterator.State.NOT_READY;
      T result = NullnessCasts.uncheckedCastNullableTToT(this.next);
      this.next = null;
      return result;
   }

   @Override
   public final void remove() {
      throw new UnsupportedOperationException();
   }

   private enum State {
      READY,
      NOT_READY,
      DONE,
      FAILED;
   }
}
