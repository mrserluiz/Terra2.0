package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class AbstractSequentialIterator<T> extends UnmodifiableIterator<T> {
   private @Nullable T nextOrNull;

   protected AbstractSequentialIterator(@Nullable T firstOrNull) {
      this.nextOrNull = firstOrNull;
   }

   protected abstract @Nullable T computeNext(T previous);

   @Override
   public final boolean hasNext() {
      return this.nextOrNull != null;
   }

   @Override
   public final T next() {
      if (this.nextOrNull == null) {
         throw new NoSuchElementException();
      }

      T oldNext = this.nextOrNull;
      this.nextOrNull = this.computeNext(oldNext);
      return oldNext;
   }
}
