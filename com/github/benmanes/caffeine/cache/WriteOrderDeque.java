package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

final class WriteOrderDeque<E extends WriteOrderDeque.WriteOrder<E>> extends AbstractLinkedDeque<E> {
   @Override
   public boolean contains(Object o) {
      return o instanceof WriteOrderDeque.WriteOrder && this.contains((WriteOrderDeque.WriteOrder<?>)o);
   }

   boolean contains(WriteOrderDeque.WriteOrder<?> e) {
      return e.getPreviousInWriteOrder() != null || e.getNextInWriteOrder() != null || e == this.first;
   }

   @Override
   public boolean remove(Object o) {
      return o instanceof WriteOrderDeque.WriteOrder && this.remove((E)o);
   }

   public boolean remove(E e) {
      if (this.contains(e)) {
         this.unlink(e);
         return true;
      } else {
         return false;
      }
   }

   public @Nullable E getPrevious(E e) {
      return e.getPreviousInWriteOrder();
   }

   public void setPrevious(E e, @Nullable E prev) {
      e.setPreviousInWriteOrder(prev);
   }

   public @Nullable E getNext(E e) {
      return e.getNextInWriteOrder();
   }

   public void setNext(E e, @Nullable E next) {
      e.setNextInWriteOrder(next);
   }

   interface WriteOrder<T extends WriteOrderDeque.WriteOrder<T>> {
      @Nullable T getPreviousInWriteOrder();

      void setPreviousInWriteOrder(@Nullable T prev);

      @Nullable T getNextInWriteOrder();

      void setNextInWriteOrder(@Nullable T next);
   }
}
