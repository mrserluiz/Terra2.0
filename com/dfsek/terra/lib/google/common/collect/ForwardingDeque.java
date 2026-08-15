package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Deque;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class ForwardingDeque<E> extends ForwardingQueue<E> implements Deque<E> {
   protected ForwardingDeque() {
   }

   protected abstract Deque<E> delegate();

   @Override
   public void addFirst(@ParametricNullness E e) {
      this.delegate().addFirst(e);
   }

   @Override
   public void addLast(@ParametricNullness E e) {
      this.delegate().addLast(e);
   }

   @Override
   public Iterator<E> descendingIterator() {
      return this.delegate().descendingIterator();
   }

   @ParametricNullness
   @Override
   public E getFirst() {
      return this.delegate().getFirst();
   }

   @ParametricNullness
   @Override
   public E getLast() {
      return this.delegate().getLast();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean offerFirst(@ParametricNullness E e) {
      return this.delegate().offerFirst(e);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean offerLast(@ParametricNullness E e) {
      return this.delegate().offerLast(e);
   }

   @Override
   public @Nullable E peekFirst() {
      return this.delegate().peekFirst();
   }

   @Override
   public @Nullable E peekLast() {
      return this.delegate().peekLast();
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable E pollFirst() {
      return this.delegate().pollFirst();
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable E pollLast() {
      return this.delegate().pollLast();
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public E pop() {
      return this.delegate().pop();
   }

   @Override
   public void push(@ParametricNullness E e) {
      this.delegate().push(e);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public E removeFirst() {
      return this.delegate().removeFirst();
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public E removeLast() {
      return this.delegate().removeLast();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean removeFirstOccurrence(@Nullable Object o) {
      return this.delegate().removeFirstOccurrence(o);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean removeLastOccurrence(@Nullable Object o) {
      return this.delegate().removeLastOccurrence(o);
   }
}
