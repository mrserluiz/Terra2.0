package com.github.benmanes.caffeine.cache;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

abstract class AbstractLinkedDeque<E> extends AbstractCollection<E> implements LinkedDeque<E> {
   @Nullable E first;
   @Nullable E last;
   int modCount;

   void linkFirst(E e) {
      E f = this.first;
      this.first = e;
      if (f == null) {
         this.last = e;
      } else {
         this.setPrevious(f, e);
         this.setNext(e, f);
      }

      this.modCount++;
   }

   void linkLast(E e) {
      E l = this.last;
      this.last = e;
      if (l == null) {
         this.first = e;
      } else {
         this.setNext(l, e);
         this.setPrevious(e, l);
      }

      this.modCount++;
   }

   E unlinkFirst() {
      E f = Objects.requireNonNull(this.first);
      E next = this.getNext(f);
      this.setNext(f, null);
      this.first = next;
      if (next == null) {
         this.last = null;
      } else {
         this.setPrevious(next, null);
      }

      this.modCount++;
      return f;
   }

   E unlinkLast() {
      E l = Objects.requireNonNull(this.last);
      E prev = this.getPrevious(l);
      this.setPrevious(l, null);
      this.last = prev;
      if (prev == null) {
         this.first = null;
      } else {
         this.setNext(prev, null);
      }

      this.modCount++;
      return l;
   }

   void unlink(E e) {
      E prev = this.getPrevious(e);
      E next = this.getNext(e);
      if (prev == null) {
         this.first = next;
      } else {
         this.setNext(prev, next);
         this.setPrevious(e, null);
      }

      if (next == null) {
         this.last = prev;
      } else {
         this.setPrevious(next, prev);
         this.setNext(e, null);
      }

      this.modCount++;
   }

   @Override
   public boolean isEmpty() {
      return this.first == null;
   }

   void checkNotEmpty() {
      if (this.isEmpty()) {
         throw new NoSuchElementException();
      }
   }

   @Override
   public int size() {
      int size = 0;

      for (E e = this.first; e != null; e = this.getNext(e)) {
         size++;
      }

      return size;
   }

   @Override
   public void clear() {
      E e = this.first;

      while (e != null) {
         E next = this.getNext(e);
         this.setPrevious(e, null);
         this.setNext(e, null);
         e = next;
      }

      this.first = this.last = null;
      this.modCount++;
   }

   @Override
   public abstract boolean contains(Object o);

   @Override
   public boolean isFirst(@Nullable E e) {
      return e != null && e == this.first;
   }

   @Override
   public boolean isLast(@Nullable E e) {
      return e != null && e == this.last;
   }

   @Override
   public void moveToFront(E e) {
      if (e != this.first) {
         this.unlink(e);
         this.linkFirst(e);
      }
   }

   @Override
   public void moveToBack(E e) {
      if (e != this.last) {
         this.unlink(e);
         this.linkLast(e);
      }
   }

   @Override
   public @Nullable E peek() {
      return this.peekFirst();
   }

   @Override
   public @Nullable E peekFirst() {
      return this.first;
   }

   @Override
   public @Nullable E peekLast() {
      return this.last;
   }

   @Override
   public E getFirst() {
      this.checkNotEmpty();
      return Objects.requireNonNull(this.peekFirst());
   }

   @Override
   public E getLast() {
      this.checkNotEmpty();
      return Objects.requireNonNull(this.peekLast());
   }

   @Override
   public E element() {
      return this.getFirst();
   }

   @Override
   public boolean offer(E e) {
      return this.offerLast(e);
   }

   @Override
   public boolean offerFirst(E e) {
      Objects.requireNonNull(e);
      if (this.contains(e)) {
         return false;
      }

      this.linkFirst(e);
      return true;
   }

   @Override
   public boolean offerLast(E e) {
      Objects.requireNonNull(e);
      if (this.contains(e)) {
         return false;
      }

      this.linkLast(e);
      return true;
   }

   @Override
   public boolean add(E e) {
      return this.offerLast(e);
   }

   @Override
   public void addFirst(E e) {
      if (!this.offerFirst(e)) {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public void addLast(E e) {
      if (!this.offerLast(e)) {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public @Nullable E poll() {
      return this.pollFirst();
   }

   @Override
   public @Nullable E pollFirst() {
      return this.isEmpty() ? null : this.unlinkFirst();
   }

   @Override
   public @Nullable E pollLast() {
      return this.isEmpty() ? null : this.unlinkLast();
   }

   @Override
   public E remove() {
      return this.removeFirst();
   }

   @Override
   public E removeFirst() {
      this.checkNotEmpty();
      return Objects.requireNonNull(this.pollFirst());
   }

   @Override
   public abstract boolean remove(Object o);

   @Override
   public boolean removeFirstOccurrence(Object o) {
      return this.remove(o);
   }

   @Override
   public E removeLast() {
      this.checkNotEmpty();
      return Objects.requireNonNull(this.pollLast());
   }

   @Override
   public boolean removeLastOccurrence(Object o) {
      return this.remove(o);
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      boolean modified = false;

      for (Object o : c) {
         modified |= this.remove(o);
      }

      return modified;
   }

   @Override
   public void push(E e) {
      this.addFirst(e);
   }

   @Override
   public E pop() {
      return this.removeFirst();
   }

   @Override
   public LinkedDeque.PeekingIterator<E> iterator() {
      return new AbstractLinkedDeque<E>.AbstractLinkedIterator(this.first) {
         @Override
         @Nullable E computeNext() {
            return (E)AbstractLinkedDeque.this.getNext(Objects.requireNonNull((E)this.cursor));
         }
      };
   }

   @Override
   public LinkedDeque.PeekingIterator<E> descendingIterator() {
      return new AbstractLinkedDeque<E>.AbstractLinkedIterator(this.last) {
         @Override
         @Nullable E computeNext() {
            return (E)AbstractLinkedDeque.this.getPrevious(Objects.requireNonNull((E)this.cursor));
         }
      };
   }

   abstract class AbstractLinkedIterator implements LinkedDeque.PeekingIterator<E> {
      @Nullable Object previous;
      @Nullable Object cursor;
      int expectedModCount = AbstractLinkedDeque.this.modCount;

      AbstractLinkedIterator(@Nullable E start) {
         this.cursor = start;
      }

      @Override
      public boolean hasNext() {
         this.checkForConcurrentModification();
         return this.cursor != null;
      }

      @Override
      public @Nullable E peek() {
         return (E)this.cursor;
      }

      @Override
      public E next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         this.previous = this.cursor;
         this.cursor = this.computeNext();
         return Objects.requireNonNull((E)this.previous);
      }

      abstract @Nullable E computeNext();

      @Override
      public void remove() {
         if (this.previous == null) {
            throw new IllegalStateException();
         }

         this.checkForConcurrentModification();
         AbstractLinkedDeque.this.remove(this.previous);
         this.expectedModCount = AbstractLinkedDeque.this.modCount;
         this.previous = null;
      }

      void checkForConcurrentModification() {
         if (AbstractLinkedDeque.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         }
      }
   }
}
