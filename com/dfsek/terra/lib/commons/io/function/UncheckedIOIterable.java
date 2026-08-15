package com.dfsek.terra.lib.commons.io.function;

import java.util.Iterator;
import java.util.Objects;

final class UncheckedIOIterable<E> implements Iterable<E> {
   private final IOIterable<E> delegate;

   UncheckedIOIterable(IOIterable<E> delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate");
   }

   @Override
   public Iterator<E> iterator() {
      return new UncheckedIOIterator<>(this.delegate.iterator());
   }
}
