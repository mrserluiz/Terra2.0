package com.dfsek.terra.lib.commons.io.function;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.BaseStream;

public interface IOBaseStream<T, S extends IOBaseStream<T, S, B>, B extends BaseStream<T, B>> extends Closeable {
   default BaseStream<T, B> asBaseStream() {
      return new UncheckedIOBaseStream<>(this);
   }

   @Override
   default void close() {
      this.unwrap().close();
   }

   default boolean isParallel() {
      return this.unwrap().isParallel();
   }

   default IOIterator<T> iterator() {
      return IOIteratorAdapter.adapt(this.unwrap().iterator());
   }

   default S onClose(IORunnable closeHandler) throws IOException {
      return this.wrap(this.unwrap().onClose(() -> Erase.run(closeHandler)));
   }

   default S parallel() {
      return (S)(this.isParallel() ? this : this.wrap(this.unwrap().parallel()));
   }

   default S sequential() {
      return (S)(this.isParallel() ? this.wrap(this.unwrap().sequential()) : this);
   }

   default IOSpliterator<T> spliterator() {
      return IOSpliteratorAdapter.adapt(this.unwrap().spliterator());
   }

   default S unordered() {
      return this.wrap(this.unwrap().unordered());
   }

   B unwrap();

   S wrap(B var1);
}
