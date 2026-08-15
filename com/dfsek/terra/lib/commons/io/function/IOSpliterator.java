package com.dfsek.terra.lib.commons.io.function;

import java.util.Objects;
import java.util.Spliterator;

public interface IOSpliterator<T> {
   static <E> IOSpliterator<E> adapt(Spliterator<E> iterator) {
      return IOSpliteratorAdapter.adapt(iterator);
   }

   default Spliterator<T> asSpliterator() {
      return new UncheckedIOSpliterator<>(this);
   }

   default int characteristics() {
      return this.unwrap().characteristics();
   }

   default long estimateSize() {
      return this.unwrap().estimateSize();
   }

   default void forEachRemaining(IOConsumer<? super T> action) {
      while (this.tryAdvance(action)) {
      }
   }

   default IOComparator<? super T> getComparator() {
      return (IOComparator<? super T>)this.unwrap().getComparator();
   }

   default long getExactSizeIfKnown() {
      return this.unwrap().getExactSizeIfKnown();
   }

   default boolean hasCharacteristics(int characteristics) {
      return this.unwrap().hasCharacteristics(characteristics);
   }

   default boolean tryAdvance(IOConsumer<? super T> action) {
      return this.unwrap().tryAdvance(Objects.requireNonNull(action, "action").asConsumer());
   }

   default IOSpliterator<T> trySplit() {
      return adapt(this.unwrap().trySplit());
   }

   Spliterator<T> unwrap();
}
