package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class AbstractIterator<T> extends UnmodifiableIterator<T> {
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

   @CanIgnoreReturnValue
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

   @ParametricNullness
   public final T peek() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return NullnessCasts.uncheckedCastNullableTToT(this.next);
      }
   }

   private enum State {
      READY,
      NOT_READY,
      DONE,
      FAILED;
   }
}
