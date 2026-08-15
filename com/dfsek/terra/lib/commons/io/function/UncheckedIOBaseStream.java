package com.dfsek.terra.lib.commons.io.function;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.BaseStream;

final class UncheckedIOBaseStream<T, S extends IOBaseStream<T, S, B>, B extends BaseStream<T, B>> implements BaseStream<T, B> {
   private final S delegate;

   UncheckedIOBaseStream(S delegate) {
      this.delegate = delegate;
   }

   @Override
   public void close() {
      this.delegate.close();
   }

   @Override
   public boolean isParallel() {
      return this.delegate.isParallel();
   }

   @Override
   public Iterator<T> iterator() {
      return this.delegate.iterator().asIterator();
   }

   @Override
   public B onClose(Runnable closeHandler) {
      return Uncheck.<IORunnable, S>apply(this.delegate::onClose, () -> closeHandler.run()).unwrap();
   }

   @Override
   public B parallel() {
      return this.delegate.parallel().unwrap();
   }

   @Override
   public B sequential() {
      return this.delegate.sequential().unwrap();
   }

   @Override
   public Spliterator<T> spliterator() {
      return this.delegate.spliterator().unwrap();
   }

   @Override
   public B unordered() {
      return this.delegate.unordered().unwrap();
   }
}
