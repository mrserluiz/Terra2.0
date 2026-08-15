package com.dfsek.terra.lib.commons.io;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public final class StreamIterator<E> implements Iterator<E>, AutoCloseable {
   private final Iterator<E> iterator;
   private final Stream<E> stream;
   private boolean closed;

   public static <T> StreamIterator<T> iterator(Stream<T> stream) {
      return new StreamIterator<>(stream);
   }

   private StreamIterator(Stream<E> stream) {
      this.stream = Objects.requireNonNull(stream, "stream");
      this.iterator = stream.iterator();
   }

   @Override
   public void close() {
      this.closed = true;
      this.stream.close();
   }

   @Override
   public boolean hasNext() {
      if (this.closed) {
         return false;
      }

      boolean hasNext = this.iterator.hasNext();
      if (!hasNext) {
         this.close();
      }

      return hasNext;
   }

   @Override
   public E next() {
      E next = this.iterator.next();
      if (next == null) {
         this.close();
      }

      return next;
   }
}
