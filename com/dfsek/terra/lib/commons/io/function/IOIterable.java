package com.dfsek.terra.lib.commons.io.function;

import java.io.IOException;
import java.util.Objects;

public interface IOIterable<T> {
   default void forEach(IOConsumer<? super T> action) throws IOException {
      this.iterator().forEachRemaining(Objects.requireNonNull(action));
   }

   IOIterator<T> iterator();

   default IOSpliterator<T> spliterator() {
      return IOSpliteratorAdapter.adapt(new UncheckedIOIterable<>(this).spliterator());
   }

   Iterable<T> unwrap();
}
