package com.dfsek.terra.lib.commons.io.function;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

final class UncheckedIOSpliterator<T> implements Spliterator<T> {
   private final IOSpliterator<T> delegate;

   UncheckedIOSpliterator(IOSpliterator<T> delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate");
   }

   @Override
   public int characteristics() {
      return this.delegate.characteristics();
   }

   @Override
   public long estimateSize() {
      return this.delegate.estimateSize();
   }

   @Override
   public void forEachRemaining(Consumer<? super T> action) {
      Uncheck.accept(this.delegate::forEachRemaining, action::accept);
   }

   @Override
   public Comparator<? super T> getComparator() {
      return this.delegate.getComparator().asComparator();
   }

   @Override
   public long getExactSizeIfKnown() {
      return this.delegate.getExactSizeIfKnown();
   }

   @Override
   public boolean hasCharacteristics(int characteristics) {
      return this.delegate.hasCharacteristics(characteristics);
   }

   @Override
   public boolean tryAdvance(Consumer<? super T> action) {
      return Uncheck.apply(this.delegate::tryAdvance, action::accept);
   }

   @Override
   public Spliterator<T> trySplit() {
      return Uncheck.get(this.delegate::trySplit).unwrap();
   }
}
