package com.github.benmanes.caffeine.cache;

import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

interface LinkedDeque<E> extends Deque<E> {
   boolean isFirst(@Nullable E e);

   boolean isLast(@Nullable E e);

   void moveToFront(E e);

   void moveToBack(E e);

   @Nullable E getPrevious(E e);

   void setPrevious(E e, @Nullable E prev);

   @Nullable E getNext(E e);

   void setNext(E e, @Nullable E next);

   LinkedDeque.PeekingIterator<E> iterator();

   LinkedDeque.PeekingIterator<E> descendingIterator();

   interface PeekingIterator<E> extends Iterator<E> {
      @Nullable E peek();

      static <E> LinkedDeque.PeekingIterator<E> concat(LinkedDeque.PeekingIterator<E> first, LinkedDeque.PeekingIterator<E> second) {
         return new LinkedDeque.PeekingIterator<E>() {
            @Override
            public boolean hasNext() {
               return first.hasNext() || second.hasNext();
            }

            @Override
            public E next() {
               if (first.hasNext()) {
                  return first.next();
               } else if (second.hasNext()) {
                  return second.next();
               } else {
                  throw new NoSuchElementException();
               }
            }

            @Override
            public @Nullable E peek() {
               return first.hasNext() ? first.peek() : second.peek();
            }
         };
      }

      static <E> LinkedDeque.PeekingIterator<E> comparing(LinkedDeque.PeekingIterator<E> first, LinkedDeque.PeekingIterator<E> second, Comparator<E> comparator) {
         return new LinkedDeque.PeekingIterator<E>() {
            @Override
            public boolean hasNext() {
               return first.hasNext() || second.hasNext();
            }

            @Override
            public E next() {
               if (!first.hasNext()) {
                  return second.next();
               }

               if (!second.hasNext()) {
                  return first.next();
               }

               E o1 = first.peek();
               E o2 = second.peek();
               boolean greaterOrEqual = comparator.compare(o1, o2) >= 0;
               return greaterOrEqual ? first.next() : second.next();
            }

            @Override
            public @Nullable E peek() {
               if (!first.hasNext()) {
                  return second.peek();
               }

               if (!second.hasNext()) {
                  return first.peek();
               }

               E o1 = first.peek();
               E o2 = second.peek();
               boolean greaterOrEqual = comparator.compare(o1, o2) >= 0;
               return greaterOrEqual ? first.peek() : second.peek();
            }
         };
      }
   }
}
