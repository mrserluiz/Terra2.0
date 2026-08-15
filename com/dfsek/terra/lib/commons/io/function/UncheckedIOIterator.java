package com.dfsek.terra.lib.commons.io.function;

import java.util.Iterator;
import java.util.Objects;

final class UncheckedIOIterator<E> implements Iterator<E> {
   private final IOIterator<E> delegate;

   UncheckedIOIterator(IOIterator<E> delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate");
   }

   @Override
   public boolean hasNext() {
      return Uncheck.getAsBoolean(this.delegate::hasNext);
   }

   @Override
   public E next() {
      return Uncheck.get(this.delegate::next);
   }

   @Override
   public void remove() {
      Uncheck.run(this.delegate::remove);
   }
}
