package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public interface IOIterator<E> {
   static <E> IOIterator<E> adapt(Iterable<E> iterable) {
      return IOIteratorAdapter.adapt(iterable.iterator());
   }

   static <E> IOIterator<E> adapt(Iterator<E> iterator) {
      return IOIteratorAdapter.adapt(iterator);
   }

   default Iterator<E> asIterator() {
      return new UncheckedIOIterator<>(this);
   }

   default void forEachRemaining(IOConsumer<? super E> action) throws IOException {
      Objects.requireNonNull(action);

      while (this.hasNext()) {
         action.accept(this.next());
      }
   }

   boolean hasNext() throws IOException;

   E next() throws IOException;

   default void remove() throws IOException {
      this.unwrap().remove();
   }

   Iterator<E> unwrap();
}
